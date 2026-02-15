## JArena - Axente Faiz ##

O projeto, desenvolvido por Deborah Ely e Kauã Perosso, utiliza a seguinte lógica de agentes: todos os agentes se movimentam em busca de cogumelos, sendo que inicialmente 5% do número de agentes fica parado a fim de ser a base inicial. 

Quando um cogumelo é encontrado, o agente com maior nível de energia entre todos permanece parado, enquanto os demais continuam buscando mais fontes de energia. Esse processo é repetido continuamente, de forma que, a cada nova coleta, o agente com maior energia assume a posição fixa.

Além disso, quando um agente encontra uma fonte de energia, ele envia uma mensagem para os agentes que estiverem no raio de comunicação, informando a coordenada onde o cogumelo foi localizado, para assim seguir em direção dele e recuperar energia.