**APDC 2023/2024**

**Projeto individual APDC-PEI 23/24**

**Projeto: GitHub Rep - verificar como obter o URL para clonar para o ambiente local:**
URL: https://github.com/DavideRodriguesUniversidade/APDC-2024-Individual.git

* Clonar o projeto para o workspace no eclipse 
* Ex: git clone https://github.com/DavideRodriguesUniversidade/APDC-2024-Individual
* Criar depois o projeto no eclipse (Maven)

**Testar o deploy local**
mvn package

mvn package appengine:run

**Testar o deploy remotamente**

mvn package appengine:deploy -Dapp.deploy.projectId=individualproject-418713 -Dapp.deploy.version=versao





