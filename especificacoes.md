# ESPECIFICAÇÃO DE REQUISITOS 

## Projeto: Jogo de tabuleiro inspirado em Carcassonne 

Documento de escopo e requisitos de software 

Versão 1.0 

2026 

Especificação de Requisitos — Projeto Carcassonne 

## **1. Introdução** 

### **1.1 Objetivo do documento** 

Este documento define o escopo inicial e os requisitos do projeto de implementação de um jogo de tabuleiro digital baseado nas regras centrais de Carcassonne. Seu objetivo é estabelecer uma referência comum para análise, projeto, implementação e testes, reduzindo ambiguidades sobre as funcionalidades que deverão compor a primeira versão do sistema. 

### **1.2 Visão geral do sistema** 

O sistema deverá permitir a realização de uma partida local entre múltiplos jogadores, em um tabuleiro construído dinamicamente por meio da colocação de peças. A aplicação será responsável por controlar os turnos, validar as jogadas, gerenciar meeples, identificar estruturas conectadas, calcular pontuações e determinar o resultado da partida. 

### **1.3 Premissas iniciais** 

- A primeira versão será voltada a partidas locais (hot-seat), com os jogadores compartilhando o mesmo dispositivo. 

- O projeto priorizará as regras do jogo-base e não incluirá expansões na versão inicial. 

- A lógica das regras deverá ser independente da camada de interface gráfica. 

- Campos e fazendeiros poderão ser tratados como requisito desejável, caso o cronograma torne necessário reduzir o escopo do MVP. 

## **2. Escopo do projeto** 

### **2.1 Escopo incluído** 

- Criação e execução de uma partida completa entre múltiplos jogadores. 

- Construção dinâmica do tabuleiro por colocação de peças. 

- Rotação das peças em incrementos de 90 graus. 

- Validação automática de posição, adjacência e compatibilidade das bordas. 

- Gerenciamento do monte de peças e da ordem de turnos. 

- Colocação e recuperação de meeples. 

- Identificação de cidades, estradas, mosteiros e, se incluído no MVP final, campos conectados. 

- Detecção automática de estruturas concluídas. 

- Cálculo e atualização automática das pontuações. 

- Pontuação de fim de partida e determinação do vencedor. 

- Interface gráfica para visualização do tabuleiro, jogador atual, pontuação e recursos disponíveis. 

### **2.2 Fora do escopo inicial** 

- Expansões oficiais ou variantes avançadas do jogo. 

- Multiplayer pela Internet ou por rede local. 

- Sistema de contas de usuário. 

- Ranking persistente entre partidas. 

- Inteligência artificial avançada. 

- Replay completo de partidas. 

Especificação de Requisitos — Projeto Carcassonne 

#### ● Compatibilidade com dispositivos móveis como requisito obrigatório. 

### **2.3 Evoluções possíveis** 

Após a conclusão e estabilização do núcleo do jogo, poderão ser adicionadas funcionalidades como salvamento e carregamento de partidas, histórico de jogadas, multiplayer em rede, inteligência artificial, replay, animações e suporte a novas regras. 

## **3. Atores e entidades principais** 

### **3.1 Atores** 

|Ator|Responsabilidade|
|---|---|
|Jogador|Participa da partida, posiciona peças, decide rotações,<br>coloca meeples e acompanha sua pontuação.|
|Sistema|Controla o estado da partida, valida ações, calcula<br>pontuação egarante a aplicação das regras.|



### **3.2 Entidades de domínio** 

|Entidade|Descrição|
|---|---|
|Partida|Mantém jogadores, turno atual, monte, tabuleiro e<br>estado geral.|
|Jogador|Mantém identificação, cor, pontuação e meeples<br>disponíveis.|
|Tabuleiro|Mantém aspeçasposicionadas e suas coordenadas.|
|Peça|Representa um tile, sua orientação e as regiões que o<br>compõem.|
|Região/Estrutura|Representa cidade, estrada, campo ou mosteiro e suas<br>conexões.|
|Meeple|Representa um seguidor associado a um jogador e a<br>uma estrutura.|
|Jogada|Representa uma ação de posicionamento, incluindo<br>peça, posição, rotação e eventual meeple.|



## **4. Requisitos funcionais** 

### **4.1 Gerenciamento da partida** 

|ID|Requisito|Descrição|
|---|---|---|
|RF01|Criar partida|O sistema deve permitir iniciar uma<br>nova partida.|
|RF02|Configurar jogadores|O sistema deve permitir definir entre<br>2 e 5 jogadores, configurando nome<br>e cor exclusiva para cada um.|
|RF03|Definir ordem de turnos|O sistema deve definir e manter a<br>ordem dos jogadores durante toda a<br>partida.|
|RF04|Indicar jogador atual|O sistema deve indicar claramente<br>qualjogadorpossui o turno atual.|



Especificação de Requisitos — Projeto Carcassonne 

|ID|Requisito|Descrição|
|---|---|---|
|RF05|Encerrar partida|O sistema deve detectar o término<br>da partida quando não houver mais<br>peças disponíveis para serem<br>jogadas.|



### **4.2 Peças e tabuleiro** 

|ID|Requisito|Descrição|
|---|---|---|
|RF06|Inicializar tabuleiro|O sistema deve iniciar a partida com<br>a peça inicial posicionada no<br>tabuleiro.|
|RF07|Manter monte de peças|O sistema deve manter o conjunto<br>de peças ainda disponíveis para<br>sorteio.|
|RF08|Sortear peça|No início de cada turno, o sistema<br>deve disponibilizar ao jogador uma<br>peça retirada do monte.|
|RF09|Rotacionar peça|O jogador deve poder rotacionar a<br>peça sorteada em 0°, 90°, 180° e<br>270°.|
|RF10|Posicionar peça|O jogador deve poder escolher uma<br>posição livre para colocar a peça<br>sorteada.|
|RF11|Validar adjacência|Uma peça só pode ser colocada se<br>estiver ortogonalmente adjacente a<br>pelo menos uma peça já<br>posicionada.|
|RF12|Validar bordas|O sistema deve impedir a colocação<br>quando as bordas da nova peça<br>forem incompatíveis com as peças<br>vizinhas.|
|RF13|Impedir sobreposição|O sistema não deve permitir a<br>colocação de uma peça em uma<br>posição já ocupada.|
|RF14|Determinar jogadas válidas|O sistema deve ser capaz de<br>determinar as posições e rotações<br>válidas para a peça atualmente<br>sorteada.|



### **4.3 Meeples** 

|ID|Requisito|Descrição|
|---|---|---|
|RF15|Controlar meeples|O sistema deve manter a<br>quantidade de meeples disponíveis<br>de cada jogador.|
|RF16|Colocar meeple|Após posicionar uma peça válida, o<br>jogador deve poder colocar um<br>meeple em uma estrutura<br>pertencente à peça recém-colocada.|
|RF17|Ignorar colocação|A colocação de meeple deve ser<br>opcional.|
|RF18|Associar meeple|O sistema deve registrar a estrutura<br>em que cada meeple foi colocado.|



Especificação de Requisitos — Projeto Carcassonne 

|ID|Requisito|Descrição|
|---|---|---|
|RF19|Validar meeple|O sistema deve impedir a colocação<br>de meeple em uma estrutura<br>conectada que já possua outro<br>meeple.|
|RF20|Recuperar meeple|Quando uma cidade, estrada ou<br>mosteiro for concluído e pontuado,<br>os meeples associados devem<br>retornar aos respectivos jogadores.|



### **4.4 Estruturas conectadas** 

|ID|Requisito|Descrição|
|---|---|---|
|RF21|Identificar estradas|O sistema deve identificar todas as<br>regiões de peças pertencentes à<br>mesma estrada.|
|RF22|Identificar cidades|O sistema deve identificar todas as<br>regiões de peças pertencentes à<br>mesma cidade.|
|RF23|Identificar campos|Caso campos façam parte do<br>escopo da versão, o sistema deve<br>identificar as regiões pertencentes<br>ao mesmo campo.|
|RF24|Detectar estruturas completas|Após cada colocação, o sistema<br>deve verificar se alguma cidade,<br>estrada ou mosteiro foi completado.|
|RF25|Determinar controle|O sistema deve determinar qual<br>jogador possui a maioria de<br>meeples em uma estrutura.|
|RF26|Tratar empate|Em caso de empate na maioria de<br>meeples, todos os jogadores<br>empatados devem receber a<br>pontuação correspondente.|



### **4.5 Pontuação** 

|ID|Requisito|Descrição|
|---|---|---|
|RF27|Pontuar estradas|O sistema deve calcular<br>automaticamente a pontuação de<br>uma estrada concluída.|
|RF28|Pontuar cidades|O sistema deve calcular<br>automaticamente a pontuação de<br>uma cidade concluída,<br>considerando suas peças e<br>elementos depontuação aplicáveis.|
|RF29|Pontuar mosteiros|O sistema deve pontuar um<br>mosteiro quando todas as posições<br>adjacentes necessárias estiverem<br>ocupadas.|
|RF30|Atualizar pontuação|Sempre que uma estrutura for<br>pontuada, o sistema deve atualizar<br>a pontuação dos jogadores<br>correspondentes.|



Especificação de Requisitos — Projeto Carcassonne 

|ID|Requisito|Descrição|
|---|---|---|
|RF31|Pontuação final|Ao final da partida, o sistema deve<br>calcular a pontuação das estruturas<br>ainda incompletas conforme as<br>regras adotadas.|
|RF32|Pontuar campos|Caso campos/fazendeiros sejam<br>incluídos, o sistema deve calcular<br>sua pontuação no final da partida.|
|RF33|Determinar vencedor|Após a pontuação final, o sistema<br>deve indicar o jogador com a maior<br>pontuação.|
|RF34|Tratar empate final|O sistema deve identificar<br>corretamente partidas em que dois<br>ou mais jogadores terminem com a<br>mesma maiorpontuação.|



### **4.6 Controle de turnos** 

|ID|Requisito|Descrição|
|---|---|---|
|RF35|Restringir ações|Apenas o jogador cujo turno está<br>ativo deve poder realizar ações de<br>jogo.|
|RF36|Controlar fases do turno|O sistema deve respeitar a<br>sequência: sortear peça, posicionar<br>peça, colocar meeple<br>opcionalmente, calcular pontuação<br>e encerrar turno.|
|RF37|Impedir ações fora de ordem|O sistema deve bloquear ações<br>incompatíveis com a fase atual do<br>turno.|
|RF38|Avançar turno|Ao final das ações do turno, o<br>sistema deve transferir o controle<br>para opróximojogador.|



### **4.7 Interface** 

|ID|Requisito|Descrição|
|---|---|---|
|RF39|Exibir tabuleiro|O sistema deve apresentar<br>graficamente todas as peças<br>posicionadas durante a partida.|
|RF40|Exibir peça atual|O sistema deve mostrar claramente<br>a peça sorteada no turno atual.|
|RF41|Representar rotação|A interface deve representar<br>visualmente a orientação atual da<br>peça.|
|RF42|Indicar jogadas válidas|A interface deve permitir destacar<br>posições válidas para a colocação<br>dapeça.|
|RF43|Exibir jogadores|O sistema deve mostrar os<br>jogadores participantes e suas<br>respectivas identificações visuais.|
|RF44|Exibir pontuação|O sistema deve apresentar a<br>pontuação atual de cada jogador.|



Especificação de Requisitos — Projeto Carcassonne 

|ID|Requisito|Descrição|
|---|---|---|
|RF45|Exibir meeples|O sistema deve apresentar quantos<br>meeples cada jogador possui<br>disponíveis.|
|RF46|Destacar jogador atual|A interface deve destacar<br>claramente o jogador cujo turno está<br>em andamento.|
|RF47|Exibir resultado|Ao terminar a partida, o sistema<br>deve apresentar as pontuações<br>finais e o vencedor ou vencedores.|



## **5. Regras de negócio** 

|ID|Regra|
|---|---|
|RN01|Uma nova peça deve compartilhar pelo menos uma<br>borda com umapeçajápresente no tabuleiro.|
|RN02|Todas as bordas em contato devem possuir tipos<br>compatíveis: cidade com cidade, estrada com estrada e<br>campo com campo.|
|RN03|Umaposiçãojá ocupada nãopode receber outrapeça.|
|RN04|A peça pode ser rotacionada apenas em múltiplos de<br>90 graus.|
|RN05|Um meeple só pode ser colocado na peça posicionada<br>no turno atual.|
|RN06|Um meeple não pode ser colocado em uma estrutura<br>conectadaquejápossua meeple.|
|RN07|Cidades, estradas e mosteiros concluídos devem ser<br>pontuados durante a partida.|
|RN08|Após a pontuação de uma cidade, estrada ou mosteiro<br>concluído, os meeples correspondentes devem retornar<br>aos jogadores.|
|RN09|A pontuação de uma estrutura deve ser atribuída ao<br>jogador, ou jogadores empatados, com maior<br>quantidade de meeples na estrutura.|
|RN10|As estruturas incompletas devem ser pontuadas no<br>encerramento da partida conforme as regras da versão<br>adotada.|



## **6. Requisitos não funcionais** 

|ID|Requisito|Descrição|
|---|---|---|
|RNF01|Usabilidade|A interface deve permitir que um<br>usuário familiarizado com o jogo<br>realize uma jogada sem<br>necessidade de comandos textuais<br>ou conhecimentos técnicos.|
|RNF02|Feedback visual|O sistema deve fornecer feedback<br>visual para jogadas válidas ou<br>inválidas, pontuação, conclusão de<br>estruturas e mudança de turno.|



Especificação de Requisitos — Projeto Carcassonne 

|ID|Requisito|Descrição|
|---|---|---|
|RNF03|Consistência|Nenhuma ação disponível pela<br>interface deve permitir a criação de<br>um estado que viole as regras<br>implementadas.|
|RNF04|Desempenho|A validação de jogadas e o cálculo<br>de pontuação devem ocorrer sem<br>atraso perceptível durante uma<br>partida normal.|
|RNF05|Tabuleiro dinâmico|O sistema deve suportar<br>crescimento do tabuleiro em<br>qualquer direção sem depender de<br>uma área fixa previamente definida<br>para a partida.|
|RNF06|Separação de responsabilidades|A lógica de regras, validação,<br>pontuação e turnos deve<br>permanecer independente da<br>camada de apresentação.|
|RNF07|Testabilidade|As principais regras de negócio<br>devem poder ser verificadas por<br>testes automatizados sem depender<br>da interface gráfica.|
|RNF08|Portabilidade|A aplicação deve ser executável no<br>ambiente definido para<br>demonstração e avaliação do<br>projeto.|
|RNF09|Manutenibilidade|A organização do código deve<br>permitir a inclusão de novas peças<br>ou regras sem alterações extensas<br>no núcleo do sistema.|
|RNF10|Integridade do estado|O sistema deve manter consistência<br>entre tabuleiro, orientações,<br>estruturas conectadas, meeples,<br>pontuação, jogador atual e peças<br>restantes.|



## **7. Priorização e definição do MVP** 

Para controlar o risco de crescimento excessivo do escopo, os requisitos serão classificados em três níveis: Essencial, Desejável e Futuro/Extra. 

|Prioridade|Funcionalidade|Momento previsto|
|---|---|---|
|Essencial|Tabuleiro dinâmico e representação<br>daspeças|MVP|
|Essencial|Rotação e validação da colocação|MVP|
|Essencial|Controle de turnos e sorteio|MVP|
|Essencial|Meeples|MVP|
|Essencial|Cidades, estradas e mosteiros|MVP|
|Essencial|Detecção de conclusão e pontuação|MVP|
|Essencial|Pontuação final e vencedor|MVP|
|Desejável|Campos e fazendeiros|Após estabilização do núcleo, se<br>necessário|
|Desejável|Destaque deposições válidas|Qualidade de interface|
|Desejável|Salvar e carregar partida|Versão posterior|



Especificação de Requisitos — Projeto Carcassonne 

|Prioridade|Funcionalidade|Momento previsto|
|---|---|---|
|Extra|Histórico e replay|Evolução|
|Extra|Multiplayer em rede|Evolução|
|Extra|Inteligência artificial|Evolução|
|Extra|Expansões e novas regras|Evolução|



### **7.1 Definição do produto mínimo viável** 

MVP proposto: permitir uma partida local completa entre múltiplos jogadores, incluindo sorteio, rotação e colocação de peças, uso de meeples, validação automática das jogadas, identificação de cidades, estradas e mosteiros conectados, cálculo automático de pontuação, controle de turnos e determinação do vencedor. 

## **8. Requisitos opcionais e evolutivos** 

|ID|Requisito|Descrição|
|---|---|---|
|RO01|Salvar partida|Permitir salvar o estado de uma<br>partida em andamento.|
|RO02|Carregar partida|Permitir restaurar uma partida salva<br>anteriormente.|
|RO03|Histórico de jogadas|Manter um registro ordenado das<br>jogadas realizadas.|
|RO04|Replay|Permitir reproduzir visualmente uma<br>partida a partir do histórico.|
|RO05|Desfazer jogada|Permitir desfazer uma ação antes<br>da confirmação definitiva do turno,<br>se compatível com as regras<br>adotadas.|
|RO06|Multiplayer em rede|Permitir a participação de jogadores<br>em dispositivos distintos.|
|RO07|Sala de jogo|Permitir criar uma sala e<br>compartilhar um código de entrada<br>com outrosjogadores.|
|RO08|Inteligência artificial|Permitir que ao menos um jogador<br>seja controlado pelo computador.|
|RO09|Níveis de IA|Permitir selecionar diferentes níveis<br>de dificuldade para jogadores<br>controlados pelo computador.|
|RO10|Efeitos audiovisuais|Adicionar animações, transições e<br>efeitos sonoros para ações<br>relevantes.|
|RO11|Expansões|Permitir a inclusão futura de novas<br>peças, estruturas e regras.|



## **9. Critérios gerais de aceitação do MVP** 

- É possível iniciar uma partida e completar todos os turnos até o esgotamento do monte de peças. 

- ● O sistema rejeita colocações incompatíveis com as peças vizinhas ou em posições ocupadas. ● As quatro orientações possíveis de uma peça são tratadas corretamente. 

- Meeples só podem ser colocados quando a regra de ocupação da estrutura for satisfeita. 

Especificação de Requisitos — Projeto Carcassonne 

- Estradas, cidades e mosteiros concluídos são identificados e pontuados corretamente. 

- Meeples elegíveis retornam aos jogadores após a pontuação de estruturas concluídas. 

- A pontuação dos jogadores permanece consistente ao longo da partida. 

- Ao final, as estruturas incompletas são pontuadas conforme as regras adotadas e o vencedor é identificado. 

- As regras centrais podem ser testadas sem depender da interface gráfica. 

## **10. Diretrizes de implementação** 

A arquitetura deverá separar o modelo e as regras do jogo da interface. Como referência, o núcleo pode ser organizado em componentes como Partida, Tabuleiro, Peça, Região/Estrutura, Jogador, Meeple e Jogada. O tabuleiro deve ser representado de forma dinâmica, preferencialmente por uma estrutura esparsa indexada por coordenadas. A identificação de estruturas conectadas pode ser implementada inicialmente por busca em grafo (BFS ou DFS), abordagem simples e adequada ao porte do jogo. 

### **10.1 Ordem sugerida de desenvolvimento** 

1. Modelagem e representação das peças e regiões. 

2. Rotação de peças. 

3. Representação dinâmica do tabuleiro. 

4. Validação da colocação. 

5. Sorteio e controle de turnos. 

6. Meeples e validação de ocupação. 

7. Identificação de estruturas conectadas. 

8. Pontuação de estradas. 

9. Pontuação de cidades. 

10. Pontuação de mosteiros. 

11. Pontuação final e encerramento. 

12. Campos/fazendeiros, caso permaneçam no escopo. 

13. Aprimoramentos de interface e funcionalidades opcionais. 

Especificação de Requisitos — Projeto Carcassonne 

