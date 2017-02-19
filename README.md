# unirio-ppgi-goalservice
YAWL custom service to execute WebServices based on the aspect-oriented business process goal.

Softwares necessários:

* Java JDK7 (32 bits)
* Eclipse 4.2 em diante (Juno, Kepler, Luna ou Mars). Versão Java EE Developers de 32 bits.
* SoapUI 5.x (32 bits)
* PostgreSQL 9.x
* YAWL 3.0.1
* WSMT 2.0
* WSMX 0.5
* DotNet Framework 4.0
* Microsoft Visual Studio 2010 em diante*

**Obs1:** ambiente testado e validado somente no Windows 7. Outras versões podem ser compatíveis.

**Obs2:** recomenda-se que tenha pelo menos 4 GB de RAM disponível para poder rodar todo o ambiente.

* Visual Studio é necessário somente para edição do código fonte do projeto Pointcut Editor.

**Instalação dos softwares:** 

* Java JDK - Realize a instalação do Java JDK de acordo com as instruções constante no site da Oracle.
* Eclipse - após baixar o arquivo .zip ou .tar.gz, descompacte em um diretório de sua preferência (ex. C:\eclipse)
* SoapUI - instale de acordo com as instruções do assistente.
* PostgreSQL - instale de acordo com as instruções do assistente.
* YAWL - instale de acordo com as instruções do assistente. Instale o YAWL em um diretório de fácil acesso pelo prompt de comando (ex. C:\YAWL).
* WSMT - após baixar o arquivo .zip, descompacte em um diretório de sua preferência (ex. C:\wsmt).
* WSMX - após baixar o arquivo .zip, descompacte em um diretório de sua preferência (ex. C:\wsmx).
* DotNet Framework - instale de acordo com as instruções do assistente.
* Visual Studio - instale de acordo com as instruções do assistente. Deve-se instalar o suporte ao Windows Forms usando a linguagem C#

**Configuração dos softwares:**

*Java*

Após realizar a instalação, configure a variável de ambiente JAVA_HOME em Control Panel -> System -> Advanced System Settings -> aba Advanced -> Environment Variables. Defina para a variável o caminho de instalação do JDK.

*Eclipse*

Para poder criar WebServices usando o assistente do Eclipse, baixe as seguintes soluções:
* Apache Axis2 (http://axis.apache.org/axis2/java/core/download.html)
* Apache CXF (https://cxf.apache.org/download.html)

Após baixar e descompactar, abra o Eclipse e vá no menu Window -> Preferences. Na opção WebServices, selecione Axis2 Preferences. Na guia Axis2 Runtime selecione o diretório do Axis2. Depois vá no item CXF 2.x Preferences, e na guia CXF Preferences adicione o diretório do CXF.

Logo em seguida, vá no menu Windows -> Show View -> Servers. Clique com botão direito e selecione New -> Server. Na janela que se abre, Escolha na opçao Apache, Tomcat v7.0 Server e clique em Next. Na tela seguinte, selecione o diretório de instalação do Tomcat. O Tomcat a ser configurado será o que veio com o YAWL. Ele se encontra no diretorio de instalaçao do YAWL, pasta engine. Uma vez selecionado o diretório, clique em Finish.

*YAWL*

Após a instalação os documentos do YAWL já são reconhecidos pelo Windows. Contudo, nenhum atalho é criado para o editor e nem para a engine. Para iniciar o editor, vá no diretório de instalação do YAWL via prompt de comando e execute o arquivo YAWL.bat da seguinte forma: .\YAWL.bat editor.

Para executar a engine, execute o seguinte comando: .\YAWL.bat controlpanel

O painel de controle aberto permite controlar a inicialização e encerramento da engine do YAWL. Para acessar a parte administrativa da engine, abra um navegador e digite o endereço http://localhost:8080/resourceService. Login: admin; senha: YAWL.

Caso o YAWL Control Panel não consiga iniciar o Tomcat, entre no prompt de comando (ou no Windows PowerShell), vá até o diretório de instalação do YAWL, navegue até engine\apache-tomcat-7.0.55\bin e inicie o Tomcat usando o startup.bat. E para encerrar, use o shutdown.bat.

**Obtenção do código fonte:**

Todo o código fonte está disponível em https://github.com/herculeshssj nos seguintes repositórios:
- AspectService: https://github.com/herculeshssj/unirio-ppgi-aspectservice
- GoalService: https://github.com/herculeshssj/unirio-ppgi-goalservice
- Pointcut Editor: https://github.com/herculeshssj/unirio-ppgi-pointcut-editor
- WebServices de exemplos: https://github.com/herculeshssj/unirio-ppgi-webservices

Vá no Eclipse e abra a perspectiva Git. Realize a clonagem dos repositórios através da opção Clone a Git Repository.

Para importar os projetos para o Eclipse, clique com o botão direito em cima do repositório e selecione Import Project. Na tela que abre, clique em Next, e na tela seguinte aparecerá o projeto existente no repositório marcado. Clique em Finish para concluir a importação.

Dos quatro repositórios, somente Pointcut Editor não é um projeto Eclipse, portanto a opção de importar projeto não irá funcionar.

**Execução dos exemplos:**

Crie o diretório C:\Java\aspect e copie os diretórios rules e specs do diretório do repositório do projeto AspectService para o diretório criado.

Vá no repositório do Pointcut Editor e realize a instalação do aplicativo que se encontra na pasta Install.

Vá no repositório do GoalService e copie as pastas dist e resources para a pasta de instalação do Eclipse.

Vá no pgAdmin e conecte no PostgreSQL local. Crie um novo usuário no item Login Roles chamado yawl com a senha yawl. Crie uma nova base de dados chamada yawl e defina como owner o usuário criado.

Após isso, no Eclipse, vá na perspectiva Server. Clique duplo em cima do servidor Tomcat. Na página de configurações, vá na opção Timeouts e defina um valor alto para Start (ex. 999). Na opção Server Location, marque o item Use Tomcat installation. Terminado, salva as mudanças.

Importe os projetos AspectService, GoalService e LogApp (repositório webservices) dos seus respectivos repositórios, compile todos usando o menu Project -> Clean.

Clique com botão direito no servidor Tomcat e selecione Add and Remove. Na janela que aparece clique no botão Add All para adicionar todos os projetos. Clique em Finnish e inicie o Tomcat.

Iniciado o Tomcat, adicione o AspectService e o GoalService ao ambiente do YAWL. Acesse http://localhost:8080/resourceService com o login admin e senha YAWL. Vá na opção Service e entre com as informações listadas abaixo:

AspectService
Name: aspectService
Password: yAspect
URI: http://localhost:8080/aspectService/ib
Description: AspectService - A implementation for aspect-oriented business process modeling

GoalService:
Name: GoalService
Password: yGoal
URI: http://localhost:8080/GoalService/ib
Description: GoalService - A implementation for applying operational sematic to aspect-oriented business process modeling

Agora vá na opção Users e crie um novo usuário administrador com todas as permissões disponíveis.

Pra finalizar, vá na opção Cases e carregue os exemplos desejados. Os exemplos do YAWL estão na raiz do diretório criado ao clonar os repositórios. Faça logout e entre com o usuário criado. Vá em Cases, selecione um processo e inicie um novo caso em Lanch Case.
