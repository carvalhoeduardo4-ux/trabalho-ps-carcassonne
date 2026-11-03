/*
 * Carcassonne v8 — cenários ilustrados determinísticos, com textura artesanal.
 * Entrada: a mesma topologia já rotacionada por toRenderTile().
 * Saída: SVG em data URI; sem alterar regras, features, API ou interações.
 * Estilo e formas originais deste laboratório, sem assets comerciais.
 */

const SIZE = 240;
const SIDES = ['NORTH', 'EAST', 'SOUTH', 'WEST'];
const SIDE_ANGLE = { NORTH: 0, EAST: 90, SOUTH: 180, WEST: 270 };
const CACHE = new Map();

const COLORS = {
  grass: '#829d60', grassLight: '#abc17a', grassDark: '#537a45',
  road: '#dcc4a0', roadEdge: '#8e7558', roadShadow: '#4e503a',
  city: '#aa9774', cityGround: '#c3ad82',
  roof: '#994f3b', roofLight: '#c07952', roofDark: '#5f3d32',
  wall: '#d6c4a1', wallLight: '#f1e4c8', wallDark: '#6b6258',
  mortar: '#7d7568',
};

function hash(text) {
  let h = 2166136261;
  for (const c of text) h = Math.imul(h ^ c.charCodeAt(0), 16777619);
  return h >>> 0;
}
function random(seed) {
  let x = seed >>> 0;
  return () => ((x = (Math.imul(x, 1664525) + 1013904223) >>> 0) / 4294967296);
}
const f = n => Number(n.toFixed(1));
const pt = (x,y) => `${f(x)} ${f(y)}`;

// Arte procedural vetorial original, sem dependências ou ativos externos.
// V7 mantém a topologia determinística e adiciona linguagem gráfica mais orgânica.
function ground(seed) {
  const r = random(seed ^ 0xf37aca);
  let v = `<rect width="240" height="240" fill="#829d60"/>`;
  // Patches de gramíneas e caminhos de pigmento sem repetição de grade.
  for (let i=0; i<64; i++) {
    const x=r()*280-20, y=r()*280-20, rx=10+r()*38, ry=4+r()*20;
    const color = ['#c2c98a','#4e794d','#9ab775','#d8c689','#7a884a'][i%5];
    v+=`<ellipse cx="${f(x)}" cy="${f(y)}" rx="${f(rx)}" ry="${f(ry)}" fill="${color}" opacity="${f(.055+r()*.11)}" transform="rotate(${f((r()-.5)*120)} ${pt(x,y)})"/>`;
  }
  // Pequenas pinceladas, em vez de texturas de círculos regulares.
  for(let i=0; i<185; i++) {
    const x=r()*240, y=r()*240, w=2+r()*8, tilt=(r()-.5)*7;
    v+=`<path d="M${pt(x,y)} q${f(w*.25)} ${f(-w*.5)} ${f(w)} ${f(tilt)}" fill="none" stroke="${i%3===0?'#456b43':'#d0d49e'}" stroke-width="${f(.5+r()*.9)}" opacity="${f(.22+r()*.22)}" stroke-linecap="round"/>`;
  }
  return v;
}

function fieldMask(tile, roadSegments, uid) {
  let mask=`<mask id="f${uid}" maskUnits="userSpaceOnUse" x="0" y="0" width="240" height="240"><rect width="240" height="240" fill="white"/>`;
  (tile.cities||[]).forEach(city=>{
    const cfg=cityGeometry(city.sides||[]);
    if(cfg.d)mask+=`<path d="${cfg.d}" transform="rotate(${cfg.angle} 120 120)" fill="black" stroke="black" stroke-width="8"/>`;
  });
  for(const segment of roadSegments)mask+=`<path d="${segment}" stroke="black" stroke-width="53" fill="none"/>`;
  if(tile.hasMonastery)mask+='<ellipse cx="120" cy="121" rx="63" ry="62" fill="black"/>';
  return mask+'</mask>';
}

// Aproximação geométrica usada APENAS para posicionar detalhes decorativos.
// As regras de conectividade e encaixe continuam exclusivamente no backend.
function cityOutline(cfg){
  if(cfg.d==='M0 0 H240 V240 H0 Z')return [[0,0],[240,0],[240,240],[0,240]];
  const walls=cfg.walls.map(w=>sampleWall(w));
  if(cfg.walls.length===2){const [a,b]=walls;return [a[0],b[0],...b.slice(1),...a.slice().reverse()];}
  const edge=walls[0]||[];
  if(cfg.d.startsWith('M0 0 H240 V240 C'))return [[0,0],[240,0],[240,240],...edge.slice().reverse()];
  if(cfg.d.startsWith('M0 0 H240 V240 H0'))return [[0,0],[240,0],[240,240],[0,240]];
  return [[0,0],[240,0],...edge.slice().reverse()];
}
function distSegment(x,y,a,b){
  const dx=b[0]-a[0],dy=b[1]-a[1];
  const t=Math.max(0,Math.min(1,((x-a[0])*dx+(y-a[1])*dy)/(dx*dx+dy*dy||1)));
  return Math.hypot(x-a[0]-t*dx,y-a[1]-t*dy);
}
function insidePolygon(x,y,poly){
  let inside=false;
  for(let i=0,j=poly.length-1;i<poly.length;j=i++){
    const a=poly[i],b=poly[j];
    if(((a[1]>y)!==(b[1]>y)) && x<(b[0]-a[0])*(y-a[1])/(b[1]-a[1])+a[0])inside=!inside;
  }
  return inside;
}
function polygonDistance(x,y,poly){let d=Infinity;for(let i=0;i<poly.length;i++)d=Math.min(d,distSegment(x,y,poly[i],poly[(i+1)%poly.length]));return d;}
function sampleRoad(d){
  const nums=d.match(/-?\d+(?:\.\d+)?/g)?.map(Number);
  if(!nums||nums.length!==8)return [];
  const a=[];for(let i=0;i<=30;i++)a.push(cubic([nums[0],nums[1]],[nums[2],nums[3]],[nums[4],nums[5]],[nums[6],nums[7]],i/30));
  return a;
}
function fieldOccupancy(tile,paths){
  const cities=(tile.cities||[]).map(c=>{
    const cfg=cityGeometry(c.sides||[]);return {poly:cityOutline(cfg),angle:cfg.angle};
  });
  const roadPoints=paths.map(sampleRoad);
  return (x,y,radius=10)=>{
    if(x<radius+7||y<radius+7||x>233-radius||y>233-radius)return false;
    for(const city of cities){
      const a=-city.angle*Math.PI/180,dx=x-120,dy=y-120;
      const xx=120+dx*Math.cos(a)-dy*Math.sin(a),yy=120+dx*Math.sin(a)+dy*Math.cos(a);
      if(insidePolygon(xx,yy,city.poly)||polygonDistance(xx,yy,city.poly)<radius+5)return false;
    }
    for(const line of roadPoints){
      for(let i=1;i<line.length;i++)if(distSegment(x,y,line[i-1],line[i])<radius+24)return false;
    }
    if(tile.hasMonastery&&Math.hypot((x-120)*.97,y-123)<radius+66)return false;
    return true;
  };
}

function oneTree(x,y,s,r) {
  const shade = ['#365e3e','#47754c','#537a42','#678349'][Math.floor(r()*4)];
  const dark = '#355840';
  let art=`<g transform="translate(${pt(x,y)}) rotate(${f(r()*55-27)}) scale(${f(s)})">`;
  art+='<ellipse cx="3" cy="6" rx="14" ry="11" fill="#29472c" opacity=".25"/>';
  art+=`<path d="M-1 -3 L1 8" stroke="#77553c" stroke-width="3" stroke-linecap="round"/>`;
  // Copa irregular com volumes, sem simetria geométrica rígida.
  art+=`<path d="M-13 -1 Q-17 -10 -9 -13 Q-10 -22 1 -18 Q11 -21 13 -12 Q22 -7 15 0 Q17 11 7 12 Q0 18 -8 10 Q-18 9 -13 -1 Z" fill="${dark}" stroke="#2f513c" stroke-width="1.3"/>`;
  for(const [xx,yy,rad] of [[-8,-9,8],[1,-13,10],[10,-6,9],[-6,3,9],[8,5,8]]){
    art+=`<path d="M${f(xx-rad*.8)} ${f(yy)} Q${f(xx-rad)} ${f(yy-rad*.8)} ${f(xx+2)} ${f(yy-rad*.85)} Q${f(xx+rad)} ${f(yy-rad*.65)} ${f(xx+rad*.7)} ${f(yy+2)} Q${f(xx)} ${f(yy+rad*.9)} ${f(xx-rad*.8)} ${f(yy)} Z" fill="${shade}" opacity="${f(.79+r()*.19)}"/>`;
  }
  art+='<path d="M-8 -12 Q0 -21 10 -13 M-12 -2 Q-8 -9 -2 -7" stroke="#c2d69c" opacity=".56" stroke-width="1.7" fill="none" stroke-linecap="round"/>';
  return art+'</g>';
}
function oneRock(x,y,s,r){
  const a=(r()-.5)*50;
  return `<g transform="translate(${pt(x,y)}) rotate(${f(a)}) scale(${f(s)})"><ellipse cx="1.5" cy="3.3" rx="10.5" ry="6.5" fill="#40543e" opacity=".26"/><path d="M-9 1 L-4 -6 L3 -8 L9 -3 L10 3 L3 6 L-7 5 Z" fill="#7e8675" stroke="#566451" stroke-width="1.5"/><path d="M-9 1 L-4 -6 L3 -8 L2 1 Z" fill="#d8d7b1" opacity=".78"/><path d="M2 1 L9 -3 L10 3 L3 6 Z" fill="#60685e" opacity=".86"/><path d="M-3 -4 L-2 1" stroke="#4c594c" stroke-width=".9"/></g>`;
}
function fieldDetails(seed,uid,tile,paths) {
  const r=random(seed ^ 0x62ef849);
  let art=`<g mask="url(#f${uid})">`;
  const valid=fieldOccupancy(tile,paths);
  // Árvores vêm em pequenos bosques, mantendo espaços visuais para a jogada.
  const anchors=[];
  for(let i=0;i<13;i++){
    let x,y,attempt=0;
    do{x=17+r()*206;y=18+r()*203;attempt++;}
    while(attempt<42&&(!valid(x,y,17)||anchors.some(p=>Math.hypot(p[0]-x,p[1]-y)<28)));
    if(!valid(x,y,17))continue;
    anchors.push([x,y]);
    const cluster = r()>.66?2:1;
    for(let k=0;k<cluster;k++) {
      const dx=k?(r()-.5)*22:0,dy=k?(r()-.5)*22:0;
      if(valid(x+dx,y+dy,15))art+=oneTree(x+dx,y+dy,.62+r()*.38,r);
    }
  }
  // Pedras de tamanhos diferentes, tufos, pequenas flores e afloramentos.
  for(let i=0;i<27;i++){
    const x=10+r()*220,y=10+r()*220;
    if(!valid(x,y,7))continue;
    if(i%3===0)art+=oneRock(x,y,.65+r()*.55,r);
    else if(i%3===1)art+=`<path d="M${pt(x-4,y+3)} q2 -8 5 -7 q-1 5 2 7 m-7 -1 q-4 -6 -6 -4" stroke="#4e7845" stroke-width="1.5" opacity=".84" fill="none" stroke-linecap="round"/>`;
    else art+=`<g transform="translate(${pt(x,y)})"><circle r="2.3" fill="${i%2?'#ecd18e':'#f4e4d0'}"/><circle cy="-.2" r=".55" fill="#a76a41"/></g>`;
  }
  // Grupos de arbustos: variam forma e quantidade de folhas.
  for(let i=0;i<13;i++){
    const x=15+r()*210,y=15+r()*210,angle=r()*350;
    if(!valid(x,y,10))continue;
    art+=`<g transform="translate(${pt(x,y)}) rotate(${f(angle)})"><path d="M-8 1 Q-9 -7 -2 -8 Q3 -13 7 -6 Q12 -4 7 4 Q0 9 -8 1 Z" fill="#446e41" opacity=".82"/><path d="M-7 -2 Q-2 -9 4 -7 M1 2 L7 -1" stroke="#a5c27d" opacity=".8" stroke-width="1.6" fill="none"/></g>`;
  }
  return art+'</g>';
}

function cityGeometry(sides) {
  const has = d => sides.includes(d);
  if (sides.length === 4) return {
    d:'M0 0 H240 V240 H0 Z', walls:[], angle:0, shield:[115,85]
  };
  if (sides.length === 1) return {
    d:'M0 0 H240 C194 42 174 102 120 103 C66 102 46 42 0 0 Z',
    walls:['M0 0 C46 42 66 102 120 103 C174 102 194 42 240 0'],
    angle:SIDE_ANGLE[sides[0]], shield:[120,42]
  };
  if (sides.length === 3) {
    // Base N,E,W (sem S); rotação determinada pelo lado ausente.
    const missing=SIDES.find(d=>!has(d));
    const angles={SOUTH:0,WEST:90,NORTH:180,EAST:270};
    return {
      d:'M0 0 H240 V240 C187 188 153 185 120 198 C87 185 53 188 0 240 Z',
      walls:['M0 240 C53 188 87 185 120 198 C153 185 187 188 240 240'],
      angle:angles[missing], shield:[117,74]
    };
  }
  if (sides.length === 2) {
    const ia=SIDES.indexOf(sides[0]),ib=SIDES.indexOf(sides[1]);
    if(Math.abs(ia-ib)===2) return {
      // Cidade em lados opostos: a borda compartilhada precisa ser inteiramente urbana.
      // A geometria afunila apenas no miolo, preservando campos laterais sem quebrar o encaixe visual.
      d:'M0 0 H240 C211 54 177 88 167 120 C177 152 211 186 240 240 H0 C29 186 63 152 73 120 C63 88 29 54 0 0 Z',
      walls:['M0 0 C29 54 63 88 73 120 C63 152 29 186 0 240',
        'M240 0 C211 54 177 88 167 120 C177 152 211 186 240 240'],
      angle:has('NORTH')?0:90,shield:[120,68]
    };
    // Base N,E; corte diagonal, cidades contíguas em duas bordas.
    const start=SIDES.find(d=>has(d)&&has(SIDES[(SIDES.indexOf(d)+1)%4]));
    return {
      d:'M0 0 H240 V240 C204 190 164 155 131 120 C96 80 57 35 0 0 Z',
      walls:['M0 0 C57 35 96 80 131 120 C164 155 204 190 240 240'],
      angle:SIDE_ANGLE[start],shield:[161,61]
    };
  }
  return {d:'',walls:[],angle:0,shield:[120,60]};
}

function cubic(a,b,c,d,t){const m=1-t;return [m*m*m*a[0]+3*m*m*t*b[0]+3*m*t*t*c[0]+t*t*t*d[0],m*m*m*a[1]+3*m*m*t*b[1]+3*m*t*t*c[1]+t*t*t*d[1]];}
function sampleWall(d) {
  // O conjunto de curvas é fixo e explícito, sem parser SVG e sem heurística visual.
  if (d.startsWith('M0 0 C46')) return sampleCubic([[0,0],[46,42],[66,102],[120,103]],[[120,103],[174,102],[194,42],[240,0]]);
  if (d.startsWith('M0 240 C53')) return sampleCubic([[0,240],[53,188],[87,185],[120,198]],[[120,198],[153,185],[187,188],[240,240]]);
  if (d.startsWith('M0 0 C29')) return sampleCubic([[0,0],[29,54],[63,88],[73,120]],[[73,120],[63,152],[29,186],[0,240]]);
  if (d.startsWith('M240 0 C211')) return sampleCubic([[240,0],[211,54],[177,88],[167,120]],[[167,120],[177,152],[211,186],[240,240]]);
  if (d.startsWith('M0 0 C57')) return sampleCubic([[0,0],[57,35],[96,80],[131,120]],[[131,120],[164,155],[204,190],[240,240]]);
  return [];
}
function sampleCubic(c1,c2){const a=[];for(const c of [c1,c2])for(let i=0;i<=24;i++){if(a.length&&i===0)continue;a.push(cubic(...c,i/24))}return a;}
function wallDecoration(path,n) {
  const points=sampleWall(path);
  if(!points.length) return '';
  const cumulative=[0];
  for(let i=1;i<points.length;i++) cumulative.push(cumulative[i-1]+Math.hypot(points[i][0]-points[i-1][0],points[i][1]-points[i-1][1]));
  const length=cumulative.at(-1);
  function at(distance){let i=1;while(i<cumulative.length-1&&cumulative[i]<distance)i++;let lo=points[i-1],hi=points[i],span=cumulative[i]-cumulative[i-1]||1,t=(distance-cumulative[i-1])/span;return {x:lo[0]+(hi[0]-lo[0])*t,y:lo[1]+(hi[1]-lo[1])*t,dx:hi[0]-lo[0],dy:hi[1]-lo[1]};}
  let art=`<path d="${path}" fill="none" stroke="#483f39" stroke-width="21" stroke-linejoin="round" opacity=".49"/>`;
  art+=`<path d="${path}" fill="none" stroke="${COLORS.wallDark}" stroke-width="17" stroke-linejoin="round"/>`;
  art+=`<path d="${path}" fill="none" stroke="${COLORS.wall}" stroke-width="13" stroke-linejoin="round"/>`;
  art+=`<path d="${path}" fill="none" stroke="${COLORS.wallLight}" stroke-width="3.6" stroke-linejoin="round" opacity=".84"/>`;
  // Juntas de pedra ortogonais à curva, cada ~16 unidades.
  for(let d=13;d<length-8;d+=16){const p=at(d),l=Math.hypot(p.dx,p.dy)||1,nx=-p.dy/l,ny=p.dx/l;
    art+=`<path d="M${pt(p.x-6.8*nx,p.y-6.8*ny)} L${pt(p.x+6.8*nx,p.y+6.8*ny)}" stroke="${COLORS.mortar}" stroke-width="1.9" opacity=".8"/>`;
  }
  // Merlões, intervalos regulares: o ritmo é visual e não altera encaixes.
  for(let d=12;d<length-9;d+=22){const p=at(d),dir=Math.atan2(p.dy,p.dx)*180/Math.PI;
    art+=`<g transform="translate(${pt(p.x,p.y)}) rotate(${f(dir)})"><rect x="-5.7" y="-12.8" width="11.4" height="5.2" rx="1" fill="${COLORS.wallLight}" stroke="${COLORS.wallDark}" stroke-width="1.1"/><path d="M-4 -10.2 H4" stroke="#fff4d4" stroke-width="1.2"/></g>`;
  }
  // Torres quadradas com parapeito crenelado e centro sombreado.
  for(const fraction of n===1?[.31,.70]:[.38,.76]){
    const p=at(length*fraction);const ang=Math.atan2(p.dy,p.dx)*180/Math.PI;
    art+=`<g transform="translate(${pt(p.x,p.y)}) rotate(${f(ang)})"><rect x="-14" y="-13" width="28" height="26" rx="2" fill="#685f52" stroke="#443e39" stroke-width="2.3"/><rect x="-11.5" y="-10.5" width="23" height="21" fill="#c7b494" stroke="#e8d6ba" stroke-width="2"/><rect x="-7" y="-6" width="14" height="12" fill="#766456" stroke="#493f36" stroke-width="1.2"/><path d="M-15 -15 h8 v5 h6 v-5 h8 v5 h6 v-5 h5 v9 h-33 Z" fill="#e6d3b1" stroke="#6b6157" stroke-width="1.4"/><path d="M-15 15 h8 v-5 h6 v5 h8 v-5 h6 v5 h5 v-9 h-33 Z" fill="#c9b697" stroke="#6b6157" stroke-width="1.4"/></g>`;
  }
  return art;
}

function house(seed,x,y,variant,scale=1) {
  const r=random(seed),rot=f((r()-.5)*31),w=20+r()*13,h=19+r()*9;
  const stone=['#d8c49e','#c1b290','#e0cfad'][Math.floor(r()*3)];
  const roof=['#9e4b39','#b15f42','#855346','#a7744a'][Math.floor(r()*4)];
  let v=`<g transform="translate(${pt(x,y)}) rotate(${rot}) scale(${f(scale)})">`;
  v+=`<path d="M${f(-w/2+3)} ${f(h/2+3)} L${f(w/2+4)} ${f(h/2+3)} L${f(w/2+5)} ${f(-h/2+4)}" stroke="#514538" opacity=".25" stroke-width="6" fill="none"/>`;
  if(variant===0){ // Telhado de duas águas com cumeeira irregular, telhas finas.
    v+=`<rect x="${f(-w/2)}" y="${f(-h/2)}" width="${f(w)}" height="${f(h)}" rx="2" fill="${stone}" stroke="#725843" stroke-width="1.5"/>`;
    v+=`<path d="M${f(-w/2-3)} ${f(-h/2-1)} L0 ${f(-h/2-7)} L${f(w/2+3)} ${f(-h/2)} L${f(w/2+3)} ${f(h/2+3)} L0 ${f(h/2+6)} L${f(-w/2-3)} ${f(h/2+1)} Z" fill="${roof}" stroke="#684238" stroke-width="1.8"/>`;
    v+=`<path d="M0 ${f(-h/2-6)} L0 ${f(h/2+5)}" stroke="#e9b18a" stroke-width="2" opacity=".84"/>`;
    for(let iy=-h/2+2;iy<h/2;iy+=5){v+=`<path d="M${f(-w/2+1)} ${f(iy)} L-2 ${f(iy+1)} M3 ${f(iy-1)} L${f(w/2+1)} ${f(iy)}" stroke="#d48d71" opacity=".48" stroke-width=".85"/>`;}
    v+=`<rect x="${f(w*.2)}" y="${f(-h/2-8)}" width="4.5" height="7" fill="#cbb89b" stroke="#7c6450" stroke-width="1"/>`;
  }else if(variant===1){ // Torre circular em pedra com telhado cônico e janelas.
    const rad=w*.48;
    v+=`<ellipse cy="3" rx="${f(rad+2)}" ry="${f(rad*.82)}" fill="#5e5548" opacity=".4"/>`;
    v+=`<circle r="${f(rad+1)}" fill="#dac8a7" stroke="#6e6653" stroke-width="2"/>`;
    v+=`<path d="M${f(-rad)} 0 L0 ${f(-rad-6)} L${f(rad)} 0 Q${f(rad*.7)} ${f(rad)} 0 ${f(rad*.84)} Q${f(-rad*.7)} ${f(rad)} ${f(-rad)} 0" fill="#a65942" stroke="#664437" stroke-width="1.8"/>`;
    v+=`<path d="M0 ${f(-rad-6)} L0 ${f(rad*.84)}" stroke="#d48c67" stroke-width="1.5"/>`;
    v+='<circle r="3" cy="2" fill="#6e5140"/>';
  }else if(variant===2){ // Sala comprida / armazém com roof em pedra-ardósia.
    v+=`<rect x="${f(-w*.62)}" y="${f(-h*.4)}" width="${f(w*1.24)}" height="${f(h*.8)}" fill="${stone}" stroke="#675847" stroke-width="1.7"/>`;
    v+=`<path d="M${f(-w*.65)} ${f(-h*.46)} L0 ${f(-h*.78)} L${f(w*.65)} ${f(-h*.46)} L${f(w*.65)} ${f(h*.44)} L0 ${f(h*.66)} L${f(-w*.65)} ${f(h*.44)} Z" fill="#636b67" stroke="#4a514e" stroke-width="2"/>`;
    v+=`<path d="M0 ${f(-h*.78)} V${f(h*.66)}" stroke="#c8c8ae" stroke-width="1.7"/>`;
    for(let k=-2;k<=2;k++)v+=`<path d="M${f(k*4)} ${f(-h*.4)} l${f(w*.24)} -2" stroke="#9fa79e" stroke-width=".7" opacity=".55"/>`;
  }else{ // Mercado de panos: toldo listrado.
    v+=`<rect x="${f(-w*.52)}" y="${f(-h*.45)}" width="${f(w*1.04)}" height="${f(h*.9)}" fill="#a18a6e" stroke="#6f5845" stroke-width="2"/>`;
    v+=`<path d="M${f(-w*.56)} ${f(-h*.50)} H${f(w*.56)} V${f(h*.50)} H${f(-w*.56)} Z" fill="#eee0c4" stroke="#734939" stroke-width="1.3"/>`;
    for(let xx=-w*.5;xx<w*.5;xx+=w/5)v+=`<rect x="${f(xx)}" y="${f(-h*.48)}" width="${f(w/9)}" height="${f(h*.96)}" fill="#a65e45" opacity=".86"/>`;
  }
  v+='</g>';
  return v;
}

function cityStreets(seed) {
  const r=random(seed^0x19cd983);
  let art='<path d="M-15 190 C45 165 75 128 125 118 S190 88 258 50" stroke="#8a785e" stroke-width="24" opacity=".20" fill="none"/><path d="M30 -12 C70 65 135 95 260 160" stroke="#eee0bd" stroke-width="12" opacity=".20" fill="none"/>';
  for(let i=0;i<115;i++){
    const x=r()*240,y=r()*240,s=1+r()*4;
    art+=`<path d="M${pt(x,y)} l${f(s)} ${f(s*.23)}" stroke="#837258" opacity="${f(.16+r()*.17)}" stroke-width="${f(.6+r()*.8)}"/>`;
  }
  return art;
}
function cityBuildings(seed,space,cfg) {
  const r=random(seed^0x87605f);
  let art='',positions=[];
  const polygon=cityOutline(cfg);
  const count=space===1?13:space===2?19:space===3?23:29;
  for(let i=0;i<count;i++){
    let x,y,tries=0;
    do{x=17+r()*205;y=16+r()*206;tries++;}
    while(tries<60&&(!insidePolygon(x,y,polygon)||polygonDistance(x,y,polygon)<13||positions.some(([a,b])=>Math.hypot(a-x,b-y)<27)));
    if(!insidePolygon(x,y,polygon)||polygonDistance(x,y,polygon)<13)continue;
    positions.push([x,y]);
    const variant=i%11===0?1:i%7===0?2:i%9===0?3:0;
    art+=house(seed^i*2797,x,y,variant,.67+r()*.37);
  }
  // Um campanário/torre de vigia sobre cidades amplas e muralhadas.
  if(space>=3){
    art+='<g transform="translate(116 100)"><ellipse cy="7" rx="23" ry="15" fill="#4d4335" opacity=".24"/><rect x="-12" y="-14" width="24" height="36" fill="#cfc2a1" stroke="#6e6651" stroke-width="2"/><path d="M-17 -15 L0 -35 L17 -15 Z" fill="#805043" stroke="#563d33" stroke-width="2"/><path d="M-6 4 V15 M6 4 V15" stroke="#73624f" stroke-width="3"/><path d="M0 -34 V-46 M-6 -41 H6" stroke="#d7bb83" stroke-width="2.5"/></g>';
  }
  return art;
}
function cityArt(city,index,seed,uid) {
  const cfg=cityGeometry(city.sides||[]);
  if(!cfg.d)return '';
  const clip=`c${uid}${index}`;
  let art=`<clipPath id="${clip}"><path d="${cfg.d}"/></clipPath>`;
  art+=`<g transform="rotate(${cfg.angle} 120 120)">`;
  art+=`<path d="${cfg.d}" fill="#b7a486"/>`;
  art+=`<g clip-path="url(#${clip})">`;
  art+='<rect width="240" height="240" fill="#c6b38e" opacity=".42"/>';
  art+=cityStreets(seed^index*691)+cityBuildings(seed^hash('city'+index),city.sides.length,cfg);
  art+='</g>';
  // Muros existentes: ameias, juntas, torres; acrescentamos pedra desgastada.
  cfg.walls.forEach((p,i)=>art+=wallDecoration(p,cfg.walls.length));
  if(city.shields>0){const [x,y]=cfg.shield;art+=`<g transform="translate(${pt(x,y)})"><path d="M-12 -16 H12 V3 Q8 15 0 19 Q-8 15 -12 3 Z" fill="#eedfb7" stroke="#423b38" stroke-width="2.4"/><path d="M-8 -12 H0 V14 Q-8 9 -8 1 Z" fill="#406b8d"/><path d="M0 -12 H8 V1 Q8 9 0 14 Z" fill="#bfb7a4"/><path d="M-5 -4 Q0 -10 5 -4" stroke="#f5e7cf" stroke-width="1.4" fill="none"/></g>`;}
  return art+'</g>';
}

const EDGES={NORTH:[120,0],EAST:[240,120],SOUTH:[120,240],WEST:[0,120]};
const INNER={NORTH:[0,1],EAST:[-1,0],SOUTH:[0,-1],WEST:[1,0]};
function roadSegments(roads=[], seed=0, cityCount=0) {
  const paths=[];
  let idx=0;
  for(const road of roads){
    const sides=road.sides||[];
    const q=random(seed^hash(`road:${road.id||0}:${idx++}`));
    if(sides.length===2){
      const [a,b]=sides,[p1,p2]=[EDGES[a],EDGES[b]];
      if(!p1||!p2)continue;
      const [u,v]=[INNER[a],INNER[b]];
      const opposite=(SIDES.indexOf(a)-SIDES.indexOf(b)+4)%4===2;
      const meander=(q()<.5?-1:1)*(cityCount?8+q()*10:25+q()*24);
      const d1=62+q()*20,d2=62+q()*20;
      // Em retas opostas, controles inclinam para o mesmo lado: curva suave e visível.
      // Com cidade, a variação é menor para não invadir a muralha.
      const c1=[p1[0]+u[0]*d1+u[1]*meander,p1[1]+u[1]*d1-u[0]*meander];
      const c2=opposite
        ? [p2[0]+v[0]*d2+u[1]*meander*.85,p2[1]+v[1]*d2-u[0]*meander*.85]
        : [p2[0]+v[0]*d2-v[1]*meander*.28,p2[1]+v[1]*d2+v[0]*meander*.28];
      paths.push(`M${pt(...p1)} C${pt(...c1)} ${pt(...c2)} ${pt(...p2)}`);
    }else{
      // Braços independentes convergem para o mesmo terminal visual.
      for(const side of sides){
        const p=EDGES[side],d=INNER[side];if(!p)continue;
        const skew=(q()-.5)*18;
        const c1=[p[0]+d[0]*57+d[1]*skew,p[1]+d[1]*57-d[0]*skew];
        const c2=[120-d[0]*27+skew*.3,120-d[1]*27-skew*.3];
        paths.push(`M${pt(...p)} C${pt(...c1)} ${pt(...c2)} 120 120`);
      }
    }
  }
  return paths;
}
function roadArt(paths=[], roads=[], seed=0, uid='x') {
  if(!paths.length)return '';
  let out='',mask=`<mask id="r${uid}" maskUnits="userSpaceOnUse" x="0" y="0" width="240" height="240"><rect width="240" height="240" fill="black"/>`;
  for(const d of paths){
    out+=`<path d="${d}" stroke="#344a34" stroke-width="49" fill="none" opacity=".18" stroke-linecap="round"/>`;
    out+=`<path d="${d}" stroke="#8f7856" stroke-width="42" fill="none" stroke-linecap="round"/>`;
    out+=`<path d="${d}" stroke="#cbb38c" stroke-width="34" fill="none" stroke-linecap="round"/>`;
    out+=`<path d="${d}" stroke="#e7cfab" stroke-width="24" fill="none" opacity=".72" stroke-linecap="round"/>`;
    out+=`<path d="${d}" stroke="#8c765c" stroke-width="1.6" fill="none" opacity=".34" stroke-dasharray="1.5 17" stroke-linecap="round"/>`;
    mask+=`<path d="${d}" stroke="white" stroke-width="31" fill="none" stroke-linecap="round"/>`;
  }
  mask+='</mask>';
  const r=random(seed^0xc4b68);
  let texture=`<g mask="url(#r${uid})">`;
  for(let i=0;i<100;i++){
    const x=r()*240,y=r()*240,rx=.5+r()*2.9,ry=.4+r()*1.7;
    texture+=`<ellipse cx="${f(x)}" cy="${f(y)}" rx="${f(rx)}" ry="${f(ry)}" fill="${i%3?'#7b7458':'#fbefd2'}" opacity="${f(.35+r()*.4)}" transform="rotate(${f(r()*150)} ${pt(x,y)})"/>`;
  }
  texture+='</g>';
  const separated=roads.filter(r=>r.sides?.length===1).length>=3;
  const joined=roads.some(r=>r.sides?.length>=3);
  let village='';
  if(separated||joined) {
    village=`<g><ellipse cx="120" cy="121" rx="27" ry="24" fill="#866e52" opacity=".38"/><path d="M99 108 Q119 89 141 108 L141 140 L100 139 Z" fill="#c4ab85" stroke="#6a5944" stroke-width="2"/><path d="M92 111 L120 83 L146 111 L124 117 L119 102 L102 116 Z" fill="#945641" stroke="#603b31" stroke-width="2.4"/><path d="M108 114 L115 111 L115 120 L108 121 Z M127 114 L134 111 L134 120 L127 121 Z" fill="#544b41"/><rect x="118" y="122" width="8" height="14" fill="#57483c"/></g>`;
  }
  return `<defs>${mask}</defs>`+out+texture+village;
}
function monasteryArt() {
  let art='<g><ellipse cx="122" cy="125" rx="65" ry="61" fill="#355b3d" opacity=".28"/>';
  art+='<path d="M65 119 Q83 98 92 61 Q116 51 140 63 L170 109 L166 158 L88 175 L69 148 Z" fill="#c4b58f" opacity=".52" stroke="#9a8467" stroke-width="2"/>';
  art+='<path d="M73 131 C92 133 98 163 124 167" stroke="#e2d4ae" stroke-width="15" opacity=".72" fill="none"/>';
  art+='<rect x="80" y="95" width="81" height="66" rx="3" fill="#d3bf9d" stroke="#695848" stroke-width="3"/>';
  art+='<path d="M75 99 L119 47 L169 99 L150 108 L119 72 L93 108 Z" fill="#875342" stroke="#5f4337" stroke-width="4"/>';
  art+='<path d="M77 96 L118 48 L165 96 M83 99 L121 59 L161 100" stroke="#cf8b64" stroke-width="2" opacity=".65" fill="none"/>';
  art+='<path d="M94 111 H149 V158 H94 Z" fill="#b7a17d" stroke="#685946" stroke-width="2"/><path d="M101 114 H141 V148 H101 Z" fill="#9d5b47" stroke="#704436" stroke-width="2"/>';
  for(let y=119;y<148;y+=5)art+=`<path d="M104 ${y} L140 ${y-2}" stroke="#da9476" opacity=".57" stroke-width="1.1"/>`;
  art+='<rect x="115" y="144" width="13" height="17" rx="4" fill="#59493a"/><path d="M122 53 V32 M113 41 H131" stroke="#f4e1b9" stroke-width="4" stroke-linecap="round"/>';
  for(const [x,y] of [[82,141],[159,135],[89,164]])art+=`<g transform="translate(${x} ${y})"><path d="M-3 7 V-5 M-8 -1 H2" stroke="#666653" stroke-width="1.9"/></g>`;
  return art+'</g>';
}

function renderArt(tile,key,angle=0) {
  const seed=hash(key),uid=seed.toString(36);
  const cities=tile.cities||[],roads=tile.roads||[], paths=roadSegments(roads,seed,cities.length);
  let svg=`<svg xmlns="http://www.w3.org/2000/svg" width="240" height="240" viewBox="0 0 240 240">`;
  svg+=`<g transform="rotate(${angle} 120 120)">`;
  svg+=ground(seed);
  svg+=roadArt(paths,roads,seed,uid);
  svg+=`<defs>${fieldMask(tile,paths,uid)}</defs>`;
  svg+=fieldDetails(seed,uid,tile,paths);
  cities.forEach((city,i)=>svg+=cityArt(city,i,seed,uid));
  if(tile.hasMonastery)svg+=monasteryArt();
  svg+='<path d="M1 .5 H239.5 V239.5 H.5 V1" fill="none" stroke="#47623f" stroke-width="1.2" opacity=".42"/>';
  svg+='</g></svg>';
  return `data:image/svg+xml;charset=utf-8,${encodeURIComponent(svg)}`;
}
function rotated(tile,steps){
  const turn=s=>SIDES[(SIDES.indexOf(s)+steps)%4];
  return {
    cities:(tile.cities||[]).map(c=>({...c,sides:c.sides.map(turn).sort((a,b)=>SIDES.indexOf(a)-SIDES.indexOf(b))})),
    roads:(tile.roads||[]).map(r=>({...r,sides:r.sides.map(turn).sort((a,b)=>SIDES.indexOf(a)-SIDES.indexOf(b))})),
    hasMonastery:!!tile.hasMonastery
  };
}
// Se a geometria gira, o terreno pintado e seus detalhes giram junto com a peça;
// não geramos árvores/casas aleatórias novas a cada toque de R.
function canonicalOrientation(tile){
  // toRenderTile informa a rotação real: assim o cenário também gira em peças
  // simétricas (por exemplo, estrada reta em 0° e 180°).
  const declared=['DEG_0','DEG_90','DEG_180','DEG_270'].indexOf(tile.artRotation);
  if(declared>=0){
    const local=rotated(tile,(4-declared)%4);
    return {tile:local,key:JSON.stringify(local),angle:declared*90};
  }
  // Compatibilidade com consumidores antigos que não enviem artRotation.
  const variants=[0,1,2,3].map(n=>({tile:rotated(tile,n),n}));
  for(const v of variants)v.key=JSON.stringify(v.tile);
  variants.sort((a,b)=>a.key.localeCompare(b.key)||a.n-b.n);
  return {tile:variants[0].tile,key:variants[0].key,angle:(4-variants[0].n)*90%360};
}
export function illustratedTileUri(tile){
  const {tile:canonical,key,angle}=canonicalOrientation(tile||{});
  const cacheKey=key+'@'+angle;
  let uri=CACHE.get(cacheKey);
  if(!uri){uri=renderArt(canonical,key,angle);CACHE.set(cacheKey,uri);if(CACHE.size>128)CACHE.delete(CACHE.keys().next().value);}
  return uri;
}
