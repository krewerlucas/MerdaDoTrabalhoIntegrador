const telaLogin = document.getElementById('tela-login');
const telaBemVindo = document.getElementById('tela-bem-vindo');
const formLogin = document.getElementById('form-login');
const erro = document.getElementById('erro');

function mostrarErro(mensagem) {
    erro.textContent = mensagem;
    erro.hidden = false;
}

async function carregarUsuarios() {
    const resposta = await fetch('/api/usuarios');
    const usuarios = await resposta.json();
    const corpo = document.getElementById('lista-usuarios');
    corpo.replaceChildren(...usuarios.map(usuario => {
        const linha = document.createElement('tr');
        [usuario.id, usuario.login, usuario.tipo, usuario.perfil].forEach(valor => {
            const celula = document.createElement('td');
            celula.textContent = valor;
            linha.appendChild(celula);
        });
        return linha;
    }));
}

function mostrarBemVindo(dados) {
    document.getElementById('mensagem').textContent = dados.mensagem;
    document.getElementById('usuario-nome').textContent = dados.usuario.nome;
    document.getElementById('usuario-perfil').textContent = dados.usuario.perfil;
    document.getElementById('usuario-roles').textContent = dados.usuario.roles.join(', ');
    telaLogin.hidden = true;
    telaBemVindo.hidden = false;
}

formLogin.addEventListener('submit', async (evento) => {
    evento.preventDefault();
    erro.hidden = true;

    const login = document.getElementById('login').value.trim();
    const senha = document.getElementById('senha').value;
    if (!login || !senha) {
        mostrarErro('Preencha login e senha.');
        return;
    }

    try {
        const resposta = await fetch('/api/auth/login', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({login, senha})
        });
        const dados = await resposta.json();
        if (!resposta.ok) {
            mostrarErro(dados.mensagem || 'Não foi possível entrar.');
            return;
        }
        mostrarBemVindo(dados);
        await carregarUsuarios();
    } catch (e) {
        mostrarErro('Servidor indisponível. Tente novamente.');
    }
});

document.getElementById('sair').addEventListener('click', () => {
    formLogin.reset();
    telaBemVindo.hidden = true;
    telaLogin.hidden = false;
});
