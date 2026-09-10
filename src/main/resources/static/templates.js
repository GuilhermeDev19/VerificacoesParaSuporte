// Estrutura inicial de templates padrão da equipe de Suporte Locaweb
const defaultTemplates = [
    {
        id: 'abertura-chamado',
        titulo: 'Abertura de Chamado',
        conteudo: `Olá, Boa Tarde/Noite!\n\nConforme nosso contato via chat, você informou que, \n\nDessa forma, estou abrindo este chamado para que a equipe responsável possa analisar.\n\nMantenha seus dados atualizados para facilitar um contato direto:\nhttps://ajuda.locaweb.com.br/wiki/alterar-dados-cadastrais-e-de-cobranca/\n\nAtenciosamente, Nome_Do_Analista Suporte Locaweb.\n\n------------------------------------------------\nComo está meu atendimento? Entre em contato com meu coordenador para qualquer feedback. | E-mail: jessica.almeida@locaweb.com.br/bruno.dias@locaweb.com.br\n------------------------------------------------\n\nCaso de duvidas basta entrar em contato via chat, WhatsApp: (11) 3544-0550 ou Telefones: (11) 3544-0500 São Paulo / 0800 555 932 Demais regiões, a qualquer horário.`
    },
    {
        id: 'inc-ritm',
        titulo: 'Abertura de INC ou RITM',
        conteudo: `Olá, Boa tarde/noite!\n\nAnalisando o caso relatado, foi necessário a abertura de uma solicitação interna.\n\nPedimos que aguarde novas interações neste chamado, qualquer update ou atualização será informada aqui.\n\nMantenha seus dados atualizados para facilitar um contato direto:\nhttps://ajuda.locaweb.com.br/wiki/alterar-dados-cadastrais-e-de-cobranca/\n\nAtenciosamente, Nome_Do_Analista Suporte Locaweb.\n\n------------------------------------------------\nComo está meu atendimento? Entre em contato com meu coordenador para qualquer feedback. | E-mail: jessica.almeida@locaweb.com.br/bruno.dias@locaweb.com.br\n------------------------------------------------\n\nCaso de duvidas basta entrar em contato via chat, WhatsApp: (11) 3544-0550 ou Telefones: (11) 3544-0500 São Paulo / 0800 555 932 Demais regiões, a qualquer horário.`
    },
    {
        id: 'chamado-resolvido',
        titulo: 'Chamado Resolvido',
        conteudo: `Olá, Boa tarde/noite!\n\nForam realizados os devidos ajustes, xxxxxxx\n\n(Informações realizadas)\n\nSe você considera que o assunto desta solicitação foi resolvido, É muito importante que você nos avalie , clique no botão "Finalizar Chamado". Em seguida, clique em "Sim, avaliar o chamado".\n\nMantenha seus dados atualizados para facilitar um contato direto:\nhttps://ajuda.locaweb.com.br/wiki/alterar-dados-cadastrais-e-de-cobranca/\n\nAtenciosamente, Nome_Do_Analista Suporte Locaweb.\n\n------------------------------------------------\nComo está meu atendimento? Entre em contato com meu coordenador para qualquer feedback. | E-mail: jessica.almeida@locaweb.com.br/bruno.dias@locaweb.com.br\n------------------------------------------------\n\nCaso de duvidas basta entrar em contato via chat, WhatsApp: (11) 3544-0550 ou Telefones: (11) 3544-0500 São Paulo / 0800 555 932 Demais regiões, a qualquer horário.`
    }
];

function carregarTemplates() {
    const salvos = localStorage.getItem('support_templates');
    return salvos ? JSON.parse(salvos) : defaultTemplates;
}

function salvarTemplates(templates) {
    localStorage.setItem('support_templates', JSON.stringify(templates));
}

function renderizarTemplates() {
    const container = document.getElementById('templateList');
    if (!container) return;
    
    const templates = carregarTemplates();
    container.innerHTML = '';

    templates.forEach((t, index) => {
        const card = document.createElement('div');
        card.className = 'bg-slate-800 p-5 rounded-xl border border-slate-700 flex flex-col justify-between gap-3 shadow-md';
        card.innerHTML = `
            <div>
                <input type="text" value="${t.titulo}" onchange="atualizarTitulo(${index}, this.value)" 
                    class="w-full bg-slate-900 border border-slate-700 font-bold text-sky-400 px-3 py-1.5 rounded mb-3 text-sm focus:outline-none focus:border-sky-500">
                <textarea onchange="atualizarConteudo(${index}, this.value)" 
                    class="w-full bg-slate-900 border border-slate-700 text-slate-200 text-xs p-3 rounded h-64 font-mono focus:outline-none focus:border-sky-500">${t.conteudo}</textarea>
            </div>
            <div class="flex justify-between items-center pt-2 border-t border-slate-700/50">
                <button onclick="copiarTemplate(${index})" class="bg-emerald-600 hover:bg-emerald-500 text-white font-semibold text-xs px-3 py-1.5 rounded flex items-center gap-1.5 transition">
                    <i class="fa-solid fa-copy"></i> Copiar Texto
                </button>
                <button onclick="removerTemplate(${index})" class="text-xs text-red-400 hover:text-red-300">
                    <i class="fa-solid fa-trash"></i> Excluir
                </button>
            </div>
        `;
        container.appendChild(card);
    });
}

function atualizarTitulo(index, novoTitulo) {
    const templates = carregarTemplates();
    templates[index].titulo = novoTitulo;
    salvarTemplates(templates);
}

function atualizarConteudo(index, novoConteudo) {
    const templates = carregarTemplates();
    templates[index].conteudo = novoConteudo;
    salvarTemplates(templates);
}

function copiarTemplate(index) {
    const templates = carregarTemplates();
    navigator.clipboard.writeText(templates[index].conteudo);
    alert('Texto copiado com sucesso! Não se esqueça de alterar o nome do analista e demais campos variáveis.');
}

function adicionarNovoTemplate() {
    const templates = carregarTemplates();
    templates.push({
        id: 'custom-' + Date.now(),
        titulo: 'Novo Modelo de Resposta',
        conteudo: 'Digite o texto do chamado aqui...'
    });
    salvarTemplates(templates);
    renderizarTemplates();
}

function removerTemplate(index) {
    if (!confirm('Deseja realmente excluir este modelo?')) return;
    const templates = carregarTemplates();
    templates.splice(index, 1);
    salvarTemplates(templates);
    renderizarTemplates();
}

function restaurarPadroes() {
    if (!confirm('Isso redefinirá todos os templates para os modelos padrão da Locaweb. Continuar?')) return;
    salvarTemplates(defaultTemplates);
    renderizarTemplates();
}

document.addEventListener('DOMContentLoaded', renderizarTemplates);
