<!DOCTYPE html>
<html lang="pt-br">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    
</head>
<body>

<h1>Arquivos Duplicados</h1>

<p>Aplicação Java com interface gráfica (GUI) para identificar, relatar e excluir arquivos duplicados em um diretório especificado pelo usuário. A busca é baseada no nome dos arquivos que possuem um número no final, como "arquivo 2.txt", e o conteúdo dos arquivos é comparado usando o hash SHA-256 para garantir que eles sejam realmente duplicados.</p>

<h2>Funcionalidade</h2>

<p>O objetivo da aplicação é auxiliar na identificação de arquivos duplicados, especialmente em situações onde vários arquivos recebem números no final de seus nomes, como "arquivo 2.txt", "arquivo 3.txt", etc. A aplicação realiza a verificação de duplicidade e oferece a opção de excluir essas cópias, mantendo o arquivo original intacto.</p>

<ul>
  <li><strong>Seleção de diretório:</strong> O usuário escolhe o diretório onde deseja procurar por arquivos duplicados.</li>
  <li><strong>Busca de arquivos duplicados:</strong> A aplicação encontra arquivos que possuem numeração no final do nome e verifica se o conteúdo é duplicado comparando os hashes dos arquivos.</li>
  <li><strong>Relatório de duplicados:</strong> Exibe os arquivos duplicados encontrados na interface gráfica.</li>
  <li><strong>Opção de exclusão:</strong> O usuário pode excluir as cópias duplicadas com numeração, preservando o arquivo original.</li>
  <li><strong>Geração de relatório:</strong> Após a exclusão, um relatório <code>resumo.txt</code> é salvo no diretório especificado contendo detalhes dos arquivos duplicados encontrados e excluídos.</li>
</ul>

<h2>Objetivo</h2>

<p>Essa aplicação visa facilitar a limpeza de arquivos duplicados gerados por sincronizações de arquivos em serviços de nuvem (OneDrive, Google Drive, iCloud) ou backups, que muitas vezes criam duplicatas com nomes incrementados.</p>

<h2>Requisitos</h2>

<h3>Sistema Operacional</h3>
<ul>
  <li>macOS</li>
  <li>Linux</li>
  <li>Windows - "Não testato"</li>
</ul>

<h3>Dependências</h3>

<ul>
  <li><strong>Java Development Kit (JDK)</strong>: A aplicação requer o JDK versão 17 ou superior.</li>
</ul>

<h3>Instalação do JDK no macOS</h3>

<p>No macOS, você pode instalar o JDK via <a href="https://brew.sh/">Homebrew</a>.</p>

<pre><code>
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
brew install openjdk
sudo ln -sfn /usr/local/opt/openjdk/libexec/openjdk.jdk /Library/Java/JavaVirtualMachines/openjdk.jdk
</code></pre>

<h3>Instalação do JDK no Linux</h3>

<p>No Linux, o JDK pode ser instalado diretamente via o gerenciador de pacotes.</p>

<p><strong>Para Ubuntu/Debian:</strong></p>

<pre><code>
sudo apt update
sudo apt install openjdk-17-jdk
</code></pre>

<p><strong>Para Fedora/CentOS:</strong></p>

<pre><code>
sudo dnf install java-17-openjdk-devel
</code></pre>

<p>Verifique a instalação com:</p>

<pre><code>
javac -version
</code></pre>

<h2>Estrutura do Projeto</h2>

<p>Após a compilação, o projeto conterá os seguintes arquivos:</p>

<pre><code>
.
├── ArquivosDuplicados.java        # Código-fonte principal
├── ArquivosDuplicados.class       # Arquivo compilado principal
├── ArquivosDuplicados$BrowseAction.class   # Classe interna gerada após compilação
├── ArquivosDuplicados$DeleteAction.class   # Classe interna gerada após compilação
├── ArquivosDuplicados$SearchAction.class   # Classe interna gerada após compilação
├── ArquivosDuplicados.jar         # Arquivo executável .jar
├── README.md                      # Instruções de instalação e execução
</code></pre>

<h2>Instruções de Compilação e Execução</h2>

<h3>1. Compilar a Aplicação</h3>

<ol>
  <li><strong>Clone o repositório:</strong> Primeiro, clone o repositório GitHub onde a aplicação está hospedada:</li>
  <pre><code>git clone https://github.com/robertodantasdecastro/ArquivosDuplicados.git
cd ArquivosDuplicados
  </code></pre>
  
  <li><strong>Compile o código-fonte Java:</strong> Compile o arquivo <code>ArquivosDuplicados.java</code> usando o compilador <code>javac</code>:</li>
  <pre><code>javac ArquivosDuplicados.java</code></pre>
</ol>

<h3>2. Gerar o Arquivo <code>.jar</code></h3>

<p>Após a compilação bem-sucedida, crie o arquivo <code>.jar</code> executável:</p>

<pre><code>jar cvfe ArquivosDuplicados.jar ArquivosDuplicados *.class
</code></pre>

<p>Esse comando gera o arquivo <strong>ArquivosDuplicados.jar</strong>, que pode ser executado em qualquer sistema com o <strong>Java Runtime Environment (JRE)</strong> instalado.</p>

<h3>3. Executar a Aplicação</h3>

<p>Para rodar a aplicação, utilize o seguinte comando:</p>

<pre><code>java -jar ArquivosDuplicados.jar</code></pre>

<p>Isso irá abrir a interface gráfica da aplicação onde você poderá selecionar um diretório e iniciar a busca por arquivos duplicados.</p>

<h2>Relatório de Arquivos Duplicados</h2>

<ul>
  <li>Após a execução da busca, a aplicação gera um relatório exibindo todos os arquivos duplicados encontrados com seus respectivos caminhos.</li>
  <li>Após a exclusão, um arquivo <code>resumo.txt</code> é gerado no diretório onde foi realizada a busca. Esse relatório contém:
    <ul>
      <li>A lista dos arquivos duplicados encontrados.</li>
      <li>O número de arquivos duplicados que foram excluídos.</li>
      <li>O espaço total liberado.</li>
    </ul>
  </li>
</ul>

<h2>Contribuindo</h2>

<p>Se quiser contribuir com este projeto, siga os passos abaixo:</p>

<ol>
  <li>Faça um fork do repositório.</li>
  <li>Crie um branch para suas alterações:
  <pre><code>git checkout -b minha-alteracao
  </code></pre>
  </li>
  <li>Faça suas alterações e commit:
  <pre><code>git commit -m "Descrição das alterações"
  </code></pre>
  </li>
  <li>Envie suas alterações:
  <pre><code>git push origin minha-alteracao
  </code></pre>
  </li>
  <li>Abra um Pull Request no GitHub.</li>
</ol>

<h2>Licença</h2>

<p>Este projeto está licenciado sob a <a href="LICENSE">MIT License</a>.</p>

<h2>Suporte</h2>

<p>Se você encontrar problemas ou tiver dúvidas, sinta-se à vontade para abrir uma <a href="https://github.com/robertodantasdecastro/ArquivosDuplicados/issues">issue</a> no repositório.</p>

</body>
</html>
