#  Carcassonne — Projeto de Software

> **Implementação do jogo de tabuleiro Carcassonne com suporte a Jogadores Humanos e Inteligência Artificial.**  
> Trabalho acadêmico desenvolvido para a disciplina de **Projeto de Software**.

---

##  Sobre o Projeto

Este repositório contém o desenvolvimento do jogo de tabuleiro **Carcassonne**, focado no uso de boas práticas de Engenharia de Software, arquitetura orientada a objetos, aplicação de padrões de projeto (**GRASP** e **GoF**) e implementação de módulo de **Inteligência Artificial (IA)** para jogadas autônomas.

O projeto opera como uma *software house* acadêmica, cobrindo todas as etapas do ciclo de desenvolvimento: desde a elicitação de requisitos e modelagem de diagramas UML até a entrega do software funcional.

---

##  Escopo & Funcionalidades

###  Funcionalidades Principais
- **Modos de Jogo:** Suporte para 2 a 5 jogadores (*Pass-and-Play* local: Humano vs. Humano e Humano vs. IA).
- **Gestão do Tabuleiro:** Grade bidimensional expansível dinamicamente.
- **Mecânica de Peças (*Tiles*):**
  - Sorteio e compra de peças a partir da pilha.
  - Rotação de peças (90°, 180°, 270°).
  - Destaque visual e validação rigorosa dos pontos válidos de encaixe (borda a borda).
- **Alocação de Seguidores (*Meeples*):**
  - Posicionamento de Meeples em estradas, cidades, mosteiros e campos (camponeses).
  - Trava de segurança: impede colocação de Meeple em estruturas já ocupadas.
- **Sistema de Pontuação Automático:**
  - Identificação de fechamento de estradas, cidades e mosteiros durante a partida.
  - Devolução de Meeples ao estoque do jogador após a conclusão.
  - Cálculo final de pontuação para estruturas incompletas e camponeses no término das peças.
- **Inteligência Artificial (IA):**
  - Módulo desacoplado para tomada de decisão autônoma.
  - Avaliação heurística de posição de peças e alocação estratégica de Meeples.

---

##  Estrutura do Repositório

```text
carcassonne-project/
├── .gitignore              # Arquivos e pastas ignorados pelo Git
├── README.md               # Documentação principal do projeto
├── assets/                 # Recursos visuais (sprites de tiles, meeples, sons)
├── docs/                   # Documentação do projeto e apresentações
│   ├── apresentacao-1/     # Escopo, Requisitos, Diagrama de Classes v1, Mockups
│   └── apresentacao-2/     # Padrões GRASP/GoF, Diagramas de Sequência, Relatório Final
├── src/                    # Código-fonte da aplicação
│   ├── core/               # Regras do jogo (Tabuleiro, Tile, Meeple, Pontuação)
│   ├── ai/                 # Módulo de Inteligência Artificial (Estratégias)
│   ├── ui/                 # Interface Gráfica e Interação com Usuário
│   └── main.py             # Ponto de entrada da aplicação
└── tests/                  # Testes unitários (validações de bordas e regras)
