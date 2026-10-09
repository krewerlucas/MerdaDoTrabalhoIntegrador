# Trabalho Integrador — Spring Boot + MySQL

API REST de usuários e roles com tela de login, feita para a disciplina de POO.
Stack: Java 21, Spring Boot 4, Spring Data JPA, MySQL (XAMPP), Lombok, Gradle.

## Como rodar

1. Inicie o MySQL do XAMPP (pelo app **manager-osx** → aba *Manage Servers* → *MySQL Database* → *Start*, ou pelo terminal):

   ```bash
   sudo /Applications/XAMPP/xamppfiles/xampp startmysql
   ```

   O XAMPP usa usuário `root` com **senha vazia**. O banco `trabalho_integrador` é criado automaticamente.
   Para outra senha: `export DB_PASSWORD=suasenha` (e `DB_USER` para outro usuário).

2. Use o JDK 21 e suba a aplicação:

   ```bash
   export JAVA_HOME=~/Library/Java/JavaVirtualMachines/corretto-21.0.7/Contents/Home
   ./gradlew bootRun
   ```

3. Abra <http://localhost:8080> e entre com **admin / admin123** (criado na primeira execução).

Testes (usam banco H2 em memória, não precisam do MySQL): `./gradlew test`

## Endpoints

| Método | Caminho | Descrição |
|---|---|---|
| POST | `/api/auth/login` | Login (200 ok, 401 credenciais inválidas) |
| GET | `/api/usuarios` | Lista usuários |
| GET | `/api/usuarios/{id}` | Busca por id (404 se não existir) |
| POST | `/api/usuarios` | Cria usuário (201 sem senha; 400 corpo inválido; 409 login repetido ou role inexistente) |
| POST | `/api/usuarios/importar` | Importa o arquivo configurado e devolve o resumo (200; 422 se o arquivo não pode ser lido) |
| PUT | `/api/usuarios/{id}` | Atualiza usuário |
| DELETE | `/api/usuarios/{id}` | Remove usuário (204) |
| POST | `/api/usuarios/{id}/roles/{nomeRole}` | Atribui uma role |
| GET | `/api/roles` | Lista roles |
| POST | `/api/roles` | Cria role (201) |

```bash
curl -X POST localhost:8080/api/auth/login -H 'Content-Type: application/json' \
  -d '{"login":"admin","senha":"admin123"}'

curl -X POST localhost:8080/api/usuarios -H 'Content-Type: application/json' \
  -d '{"nome":"Maria","email":"maria@escola.com","login":"maria","senha":"senha123","tipo":"CLIENTE"}'

curl localhost:8080/api/usuarios
curl localhost:8080/api/usuarios/1
curl -X POST localhost:8080/api/usuarios/2/roles/ROLE_ADMIN
curl -X POST localhost:8080/api/roles -H 'Content-Type: application/json' \
  -d '{"nome":"ROLE_PROFESSOR","descricao":"Acesso de professor"}'
```

Erros voltam em JSON: `{"status": 404, "erro": "Not Found", "mensagem": "...", "timestamp": "..."}`.

## Cadastro por arquivo e por JSON

Os dois caminhos usam o **mesmo método**: `UsuarioService.criar(UsuarioRequest)` (BCrypt, tipo, roles, login único).

- `UsuarioArquivoReader` só transforma texto em `UsuarioRequest` (não grava nada).
- `ImportacaoUsuarioService` chama o leitor e, para cada linha válida, chama `UsuarioService.criar`. Cada linha grava na sua própria transação.
- `ImportacaoUsuarioController` só chama o service de importação; quem abre o arquivo é o leitor.

### Contrato do arquivo

O caminho fica em `application.properties`: `app.usuarios.arquivo=./usuarios.txt` (relativo à pasta onde a aplicação roda). Exemplo na raiz: `usuarios.txt`.

```
tipo;login;nome;email;senha;roles
CLIENTE;maria;Maria Silva;maria@escola.com;senha123;ROLE_USER
ADMIN;joao;Joao Souza;joao@escola.com;senha123;ROLE_ADMIN,ROLE_USER
```

- Uma linha = um usuário; campos separados por `;` (na ordem acima); roles separadas por `,`. Não use `;` dentro dos campos.
- `tipo` só pode ser `ADMIN` ou `CLIENTE`. As roles precisam já existir no banco. Se `roles` ficar vazio, vale o padrão (`ROLE_USER`, e `ROLE_ADMIN` para ADMIN).
- Linha vazia é ignorada. Linha começando com `#` é comentário. O cabeçalho (`tipo;login;...`), se estiver na linha 1, também é ignorado.
- **Erro de linha** (a leitura continua): campos a mais ou a menos, tipo desconhecido, e-mail inválido ou outro campo fora das regras, login já cadastrado, role inexistente.
- **Erro do arquivo inteiro**: arquivo inexistente, sem permissão de leitura ou falha de I/O (a API responde 422).
- Os números de linha do resumo são as linhas físicas do arquivo (contando vazias e comentários). `linhasLidas` conta só as linhas com dados.

```bash
curl -X POST localhost:8080/api/usuarios/importar
# {"linhasLidas":3,"gravadas":1,"falhas":[{"linha":2,"motivo":"tipo desconhecido: ..."},{"linha":3,"motivo":"Login já cadastrado: maria"}]}

curl -X POST localhost:8080/api/usuarios -H 'Content-Type: application/json' \
  -d '{"nome":"Ana","email":"ana@escola.com","login":"ana","senha":"senha123","tipo":"CLIENTE","roles":["ROLE_USER"]}'
```

No JSON, `roles` é opcional (mesmas regras do arquivo).

### Logs

`debug`: detalhe da linha (`lendo linha 3: login=maria`) · `info`: usuário gravado e resumo da importação · `warn`: linha ou JSON inválido, login duplicado, role inexistente · `error`: só falha inesperada (banco fora, erro de I/O), com a exceção.

## Onde está cada passo do quadro

| Passo | Onde |
|---|---|
| 1. 3 métodos no model | `model/Usuario.java`: `adicionarRole`, `removerRole`, `possuiRole`, `podeGerenciarUsuarios` |
| 2. Service | `service/UsuarioServiceImpl.java`, `service/AuthService.java`, `service/RoleService.java` |
| 3. Controller REST | `controller/` — `@RestController` + `@RequestMapping("/api/...")` |
| 4. Interface | `service/UsuarioService.java` (o controller depende da interface) |
| 5. Comunicação com MySQL | `src/main/resources/application.properties` |
| 6. Interface com o banco | `repository/` — interfaces que estendem `JpaRepository` |

## Onde está cada conceito de POO

| Conceito | Onde |
|---|---|
| Herança | `Pessoa` (abstrata) → `Usuario` (abstrata) → `Administrador` / `Cliente` |
| Polimorfismo (sobrescrita) | `descreverPerfil()`, `podeGerenciarUsuarios()`, `getTipo()`; usado em `UsuarioResponse.de` ao listar `List<Usuario>` |
| Polimorfismo (sobrecarga) | `Usuario.possuiRole(String)` e `possuiRole(Role)` |
| Agregação | `Usuario` ↔ `Role` (`@ManyToMany`): a role existe sozinha e é compartilhada |
| Associação | `RegistroAcesso` → `Usuario` (`@ManyToOne`), gravado a cada tentativa de login |
| Interface | `UsuarioService` / `UsuarioServiceImpl`; repositórios |
| try/catch | `UsuarioServiceImpl.criar/atualizar/remover` (converte erro do banco em `RegraNegocioException`) |
| Exceções + tratamento global | `exception/` e `GlobalExceptionHandler` |
| Logs (debug/info/warn/error) | `@Slf4j` nos services, controller e `GlobalExceptionHandler` |

Senhas são guardadas com hash BCrypt e nunca aparecem nas respostas da API. 

## Perguntas da apresentação

### 1. Por que uma linha ruim não pode derrubar o arquivo inteiro, e o JSON inválido pode derrubar a requisição?

No arquivo, cada linha é um registro independente dos outros. Se uma linha com erro interrompesse a leitura, as linhas boas seguintes seriam perdidas e ninguém saberia quais ficaram de fora. Por isso o tratamento é por linha:

- O `UsuarioArquivoReader` trata cada linha com seu próprio `try/catch`. Campo a menos, tipo desconhecido ou e-mail inválido viram o erro daquela linha, e a leitura segue na próxima.
- O `ImportacaoUsuarioService` captura os erros de negócio de cada linha (login repetido, role inexistente) e continua o laço.
- Cada linha é gravada em sua própria transação, porque o `ImportacaoUsuarioService` não é `@Transactional` e o `UsuarioService.criar` é. Assim, uma linha que falha não desfaz as que já foram gravadas.
- No final, o resumo mostra quantas foram lidas, quantas gravadas e quais falharam, com o número da linha e o motivo.

No JSON, a requisição é **um único registro**. Se ele é inválido, não há uma parte boa para aproveitar, então a requisição inteira é rejeitada com 400 (pela Bean Validation e pelo `GlobalExceptionHandler`) e o cliente corrige e reenvia.

O erro do arquivo inteiro (arquivo inexistente ou sem permissão de leitura) é diferente: não há nenhuma linha para processar, então a importação toda é interrompida e a API responde 422.

### 3. O que acontece se o mesmo login vier no arquivo e, em seguida, no JSON?

O `UsuarioService.criar` é o único método de criação e é usado pelos dois caminhos. Ele confere se o login já existe antes de gravar:

- Se o login veio do arquivo e depois chega por JSON, o `POST /api/usuarios` responde **409** com `Login já cadastrado: <login>`.
- Se for o contrário (JSON primeiro e arquivo depois), a linha do arquivo falha com o mesmo motivo e aparece na lista de falhas do resumo, com o número da linha.
- O mesmo vale para um login repetido dentro do próprio arquivo: a primeira ocorrência grava e a segunda falha.
- A coluna `login` também é `unique` no banco, como segunda barreira, caso duas gravações aconteçam ao mesmo tempo.

Comprovado pelo teste `mesmoLoginNoArquivoEDepoisNoJsonRetorna409` e por chamadas manuais.
