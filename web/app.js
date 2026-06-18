// Estado da Aplicação
let supabaseClient = null;
let currentSession = null;
let currentView = 'dashboard';
let salesChart = null;

// Itens temporários do orçamento sendo criado (Carrinho)
let budgetCart = [];

// Elementos da Interface (DOM)
const authSection = document.getElementById('auth-section');
const appSection = document.getElementById('app-section');
const loginForm = document.getElementById('login-form');
const emailInput = document.getElementById('login-email');
const passwordInput = document.getElementById('login-password');
const loginBtn = document.getElementById('btn-login');
const loginErrorMsg = document.getElementById('login-error-message');
const errorText = document.getElementById('error-text');
const logoutBtn = document.getElementById('btn-logout');

// Inicialização ao carregar a página
document.addEventListener('DOMContentLoaded', () => {
    loadConfig();
    setupEventListeners();
});

// 1. Carregar configurações do arquivo config.json
async function loadConfig() {
    try {
        const response = await fetch('config.json');
        if (!response.ok) throw new Error('Não foi possível ler o arquivo config.json');
        
        const config = await response.json();
        
        if (!config.supabaseUrl || config.supabaseUrl.includes('SUA_URL') || 
            !config.supabaseAnonKey || config.supabaseAnonKey.includes('DIGITE_SUA_CHAVE') || 
            config.supabaseAnonKey === '') {
            showConfigWarning();
            return;
        }

        initSupabase(config.supabaseUrl, config.supabaseAnonKey);
    } catch (error) {
        console.error('Erro de configuração:', error);
        showConfigWarning(error.message);
    }
}

// Exibe aviso se não estiver configurado
function showConfigWarning(details = '') {
    authSection.innerHTML = `
        <div class="auth-card" style="max-width: 500px;">
            <div class="auth-header">
                <div class="auth-logo" style="background: linear-gradient(135deg, var(--color-orange), var(--color-red));">
                    <i class="fa-solid fa-triangle-exclamation"></i>
                </div>
                <h1>Configuração Necessária</h1>
                <p>O arquivo <strong>web/config.json</strong> precisa ser configurado com as credenciais do Supabase.</p>
            </div>
            <div style="background: rgba(255, 255, 255, 0.03); border: 1px solid var(--border-glass); padding: 16px; border-radius: var(--border-radius-sm); margin-bottom: 20px; font-size: 14px; line-height: 1.6;">
                <p style="margin-bottom: 8px;"><strong>Passo a passo:</strong></p>
                <ol style="margin-left: 20px; color: var(--text-secondary);">
                    <li>Acesse o painel do seu projeto no <strong>Supabase</strong></li>
                    <li>Vá em <strong>Project Settings > API</strong></li>
                    <li>Copie o campo <strong>Project URL</strong></li>
                    <li>Copie a chave <strong>anon / public</strong></li>
                    <li>Substitua os valores no arquivo <code>web/config.json</code></li>
                </ol>
                ${details ? `<p style="margin-top: 10px; color: var(--color-red); font-size: 12px;">Erro: ${details}</p>` : ''}
            </div>
            <button onclick="window.location.reload()" class="btn btn-secondary btn-block">
                <i class="fa-solid fa-rotate-right"></i>
                <span>Tentar Novamente</span>
            </button>
        </div>
    `;
}

// 2. Inicializar Supabase
function initSupabase(url, key) {
    try {
        const { createClient } = supabase;
        supabaseClient = createClient(url, key);
        
        supabaseClient.auth.onAuthStateChange((event, session) => {
            handleAuthStateChange(session);
        });
    } catch (e) {
        console.error('Erro ao inicializar Supabase Client:', e);
        showConfigWarning('Falha na biblioteca Supabase: ' + e.message);
    }
}

// Tratar alteração do estado de login
function handleAuthStateChange(session) {
    currentSession = session;
    
    if (session) {
        document.getElementById('user-display-sidebar').textContent = session.user.email;
        document.getElementById('user-display-mobile').textContent = session.user.email.split('@')[0];
        
        authSection.classList.add('hidden');
        appSection.classList.remove('hidden');
        
        if (loginForm) loginForm.reset();
        hideLoginError();

        // Ir para a tela padrão (Dashboard)
        switchView('dashboard');
    } else {
        appSection.classList.add('hidden');
        authSection.classList.remove('hidden');
    }
}

// Configura eventos gerais do DOM
function setupEventListeners() {
    // Formulário de Login
    if (loginForm) {
        loginForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const email = emailInput.value.trim();
            const password = passwordInput.value;
            await login(email, password);
        });
    }

    // Botão de Logout
    if (logoutBtn) {
        logoutBtn.addEventListener('click', async () => {
            await logout();
        });
    }

    // Navegação (Menu Lateral)
    document.querySelectorAll('.nav-item').forEach(btn => {
        btn.addEventListener('click', () => {
            const view = btn.getAttribute('data-view');
            switchView(view);
        });
    });

    // Navegação (Barra Móvel)
    document.querySelectorAll('.mobile-nav-item').forEach(btn => {
        if (btn.id !== 'btn-mobile-more') {
            btn.addEventListener('click', () => {
                const view = btn.getAttribute('data-view');
                switchView(view);
            });
        }
    });

    // Toggle Menu (Hamburguer Mobile)
    const toggleMenuBtn = document.getElementById('btn-toggle-menu');
    const sidebar = document.getElementById('sidebar');
    if (toggleMenuBtn) {
        toggleMenuBtn.addEventListener('click', () => {
            sidebar.classList.toggle('open');
        });
    }

    // Eventos dos Formulários Modais
    document.getElementById('client-form').addEventListener('submit', saveClient);
    document.getElementById('product-form').addEventListener('submit', saveProduct);
    document.getElementById('service-form').addEventListener('submit', saveService);
    document.getElementById('expense-form').addEventListener('submit', saveExpense);

    // Barra de Pesquisa em tempo real (Debounce)
    setupSearchFilters();

    // Criador de Orçamentos
    setupBudgetCreatorEvents();
}

// Configuração de buscas em tempo real
function setupSearchFilters() {
    const filters = [
        { inputId: 'client-search', fetchFn: fetchClients },
        { inputId: 'product-search', fetchFn: fetchProducts },
        { inputId: 'service-search', fetchFn: fetchServices },
        { inputId: 'budget-search', fetchFn: fetchBudgets },
        { inputId: 'os-search', fetchFn: fetchOS },
        { inputId: 'expense-search', fetchFn: fetchExpenses }
    ];

    filters.forEach(filter => {
        const input = document.getElementById(filter.inputId);
        let timeout = null;
        if (input) {
            input.addEventListener('input', (e) => {
                clearTimeout(timeout);
                timeout = setTimeout(() => {
                    filter.fetchFn(e.target.value.trim());
                }, 300);
            });
        }
    });
}

// Navegação do painel SPA
function switchView(viewName) {
    currentView = viewName;
    
    // Ocultar todos os painéis
    document.querySelectorAll('.view-panel').forEach(panel => {
        panel.classList.add('hidden');
    });
    
    // Exibir painel selecionado
    const targetPanel = document.getElementById(`view-${viewName}`);
    if (targetPanel) targetPanel.classList.remove('hidden');

    // Fechar sidebar móvel se aberta
    document.getElementById('sidebar').classList.remove('open');

    // Atualizar títulos e classes ativas
    const viewTitles = {
        'dashboard': 'Dashboard',
        'clients': 'Clientes',
        'products': 'Produtos',
        'services': 'Serviços',
        'budgets': 'Orçamentos',
        'os': 'Ordens de Serviço',
        'expenses': 'Despesas'
    };
    document.getElementById('current-view-title').textContent = viewTitles[viewName] || 'Sistema';

    // Menu Desktop
    document.querySelectorAll('.nav-item').forEach(btn => {
        btn.classList.remove('active');
        if (btn.getAttribute('data-view') === viewName) btn.classList.add('active');
    });

    // Menu Mobile
    document.querySelectorAll('.mobile-nav-item').forEach(btn => {
        btn.classList.remove('active');
        if (btn.getAttribute('data-view') === viewName) btn.classList.add('active');
    });

    // Disparar busca de dados correspondentes
    if (viewName === 'dashboard') fetchDashboard();
    else if (viewName === 'clients') fetchClients();
    else if (viewName === 'products') fetchProducts();
    else if (viewName === 'services') fetchServices();
    else if (viewName === 'budgets') fetchBudgets();
    else if (viewName === 'os') fetchOS();
    else if (viewName === 'expenses') fetchExpenses();
}

// Controle de Modais (Pop-ups)
function openModal(modalId) {
    // Limpar formulário e IDs
    if (modalId === 'modal-client') {
        document.getElementById('client-form').reset();
        document.getElementById('client-id-field').value = '';
        document.getElementById('client-modal-title').textContent = 'Novo Cliente';
    } else if (modalId === 'modal-product') {
        document.getElementById('product-form').reset();
        document.getElementById('product-id-field').value = '';
        document.getElementById('product-modal-title').textContent = 'Novo Produto';
    } else if (modalId === 'modal-service') {
        document.getElementById('service-form').reset();
        document.getElementById('service-id-field').value = '';
        document.getElementById('service-modal-title').textContent = 'Novo Serviço';
    } else if (modalId === 'modal-expense') {
        document.getElementById('expense-form').reset();
        document.getElementById('expense-id-field').value = '';
        document.getElementById('expense-modal-title').textContent = 'Nova Despesa';
    }
    
    document.getElementById(modalId).classList.remove('hidden');
}

function closeModal(modalId) {
    document.getElementById(modalId).classList.add('hidden');
}

// Gaveta de Navegação Mobile Drawer
function toggleMobileMenu() {
    document.getElementById('mobile-menu-overlay').classList.toggle('hidden');
}

function navigateFromDrawer(viewName) {
    toggleMobileMenu();
    switchView(viewName);
}

async function logoutFromDrawer() {
    toggleMobileMenu();
    await logout();
}

// Autenticação
async function login(email, password) {
    if (!supabaseClient) return;
    setLoadingState(true);
    hideLoginError();
    try {
        const { error } = await supabaseClient.auth.signInWithPassword({ email, password });
        if (error) throw error;
    } catch (error) {
        console.error('Erro login:', error);
        showLoginError('E-mail ou senha incorretos.');
    } finally {
        setLoadingState(false);
    }
}

async function logout() {
    if (!supabaseClient) return;
    try {
        await supabaseClient.auth.signOut();
    } catch (e) {
        console.error(e);
    }
}

function setLoadingState(isLoading) {
    if (isLoading) {
        loginBtn.disabled = true;
        loginBtn.innerHTML = `<div class="spinner" style="width: 18px; height: 18px; border-width: 2px;"></div><span>Entrando...</span>`;
    } else {
        loginBtn.disabled = false;
        loginBtn.innerHTML = `<span>Entrar</span><i class="fa-solid fa-arrow-right-to-bracket"></i>`;
    }
}

function showLoginError(msg) {
    errorText.textContent = msg;
    loginErrorMsg.classList.remove('hidden');
}

function hideLoginError() {
    loginErrorMsg.classList.add('hidden');
}

// ========================================================
// CONTROLLERS CRUD DO BANCO DE DADOS (SUPABASE)
// ========================================================

// 👥 CLIENTES
async function fetchClients(query = '') {
    if (!supabaseClient) return;
    try {
        let req = supabaseClient.from('clientes').select('*');
        if (query) req = req.ilike('nome', `%${query}%`);
        const { data, error } = await req.order('nome');
        if (error) throw error;
        
        const list = document.getElementById('clients-list');
        list.innerHTML = '';
        data.forEach(c => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td data-label="Nome">${c.nome}</td>
                <td data-label="Telefone">${c.telefone || '-'}</td>
                <td data-label="E-mail">${c.email || '-'}</td>
                <td data-label="Cidade">${c.cidade || '-'}</td>
                <td data-label="Ações" class="actions-cell">
                    <button class="action-btn edit" onclick="editClient(${c.id})" title="Editar"><i class="fa-solid fa-pen-to-square"></i></button>
                    <button class="action-btn delete" onclick="deleteClient(${c.id})" title="Excluir"><i class="fa-solid fa-trash-can"></i></button>
                </td>
            `;
            list.appendChild(tr);
        });
    } catch (e) { console.error(e); }
}

async function saveClient(e) {
    e.preventDefault();
    const id = document.getElementById('client-id-field').value;
    const client = {
        nome: document.getElementById('client-name').value.trim(),
        telefone: document.getElementById('client-phone').value.trim(),
        email: document.getElementById('client-email').value.trim(),
        endereco: document.getElementById('client-address').value.trim(),
        cidade: document.getElementById('client-city').value.trim(),
        observacoes: document.getElementById('client-obs').value.trim()
    };
    try {
        let res;
        if (id) {
            res = await supabaseClient.from('clientes').update(client).eq('id', id);
        } else {
            res = await supabaseClient.from('clientes').insert([client]);
        }
        if (res.error) throw res.error;
        closeModal('modal-client');
        fetchClients();
    } catch (e) { alert('Erro ao salvar cliente: ' + e.message); }
}

async function editClient(id) {
    try {
        const { data, error } = await supabaseClient.from('clientes').select('*').eq('id', id).single();
        if (error) throw error;
        document.getElementById('client-id-field').value = data.id;
        document.getElementById('client-name').value = data.nome;
        document.getElementById('client-phone').value = data.telefone || '';
        document.getElementById('client-email').value = data.email || '';
        document.getElementById('client-address').value = data.endereco || '';
        document.getElementById('client-city').value = data.cidade || '';
        document.getElementById('client-obs').value = data.observacoes || '';
        
        document.getElementById('client-modal-title').textContent = 'Editar Cliente';
        document.getElementById('modal-client').classList.remove('hidden');
    } catch (e) { console.error(e); }
}

async function deleteClient(id) {
    if (!confirm('Deseja realmente excluir este cliente?')) return;
    try {
        const { error } = await supabaseClient.from('clientes').delete().eq('id', id);
        if (error) throw error;
        fetchClients();
    } catch (e) { alert('Erro ao deletar cliente (verifique se possui orçamentos associados): ' + e.message); }
}

// 📦 PRODUTOS
async function fetchProducts(query = '') {
    if (!supabaseClient) return;
    try {
        let req = supabaseClient.from('produtos').select('*');
        if (query) req = req.ilike('nome', `%${query}%`);
        const { data, error } = await req.order('nome');
        if (error) throw error;
        
        const list = document.getElementById('products-list');
        list.innerHTML = '';
        data.forEach(p => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td data-label="Nome">${p.nome}</td>
                <td data-label="Estoque">${parseFloat(p.quantidade).toFixed(2).replace('.', ',')}</td>
                <td data-label="Preço Venda">${formatCurrency(p.preco_venda)}</td>
                <td data-label="Preço Custo">${formatCurrency(p.preco_custo)}</td>
                <td data-label="Unidade">${p.tipo_venda}</td>
                <td data-label="Ações" class="actions-cell">
                    <button class="action-btn edit" onclick="editProduct(${p.id})" title="Editar"><i class="fa-solid fa-pen-to-square"></i></button>
                    <button class="action-btn delete" onclick="deleteProduct(${p.id})" title="Excluir"><i class="fa-solid fa-trash-can"></i></button>
                </td>
            `;
            list.appendChild(tr);
        });
    } catch (e) { console.error(e); }
}

async function saveProduct(e) {
    e.preventDefault();
    const id = document.getElementById('product-id-field').value;
    const product = {
        nome: document.getElementById('product-name').value.trim(),
        preco_custo: parseFloat(document.getElementById('product-cost').value),
        preco_venda: parseFloat(document.getElementById('product-price').value),
        quantidade: parseFloat(document.getElementById('product-quantity').value),
        tipo_venda: document.getElementById('product-unit').value
    };
    try {
        let res;
        if (id) {
            res = await supabaseClient.from('produtos').update(product).eq('id', id);
        } else {
            res = await supabaseClient.from('produtos').insert([product]);
        }
        if (res.error) throw res.error;
        closeModal('modal-product');
        fetchProducts();
    } catch (e) { alert('Erro ao salvar produto: ' + e.message); }
}

async function editProduct(id) {
    try {
        const { data, error } = await supabaseClient.from('produtos').select('*').eq('id', id).single();
        if (error) throw error;
        document.getElementById('product-id-field').value = data.id;
        document.getElementById('product-name').value = data.nome;
        document.getElementById('product-cost').value = data.preco_custo;
        document.getElementById('product-price').value = data.preco_venda;
        document.getElementById('product-quantity').value = data.quantidade;
        document.getElementById('product-unit').value = data.tipo_venda;
        
        document.getElementById('product-modal-title').textContent = 'Editar Produto';
        document.getElementById('modal-product').classList.remove('hidden');
    } catch (e) { console.error(e); }
}

async function deleteProduct(id) {
    if (!confirm('Deseja realmente excluir este produto?')) return;
    try {
        const { error } = await supabaseClient.from('produtos').delete().eq('id', id);
        if (error) throw error;
        fetchProducts();
    } catch (e) { alert('Erro ao excluir produto: ' + e.message); }
}

// 🛠️ SERVIÇOS
async function fetchServices(query = '') {
    if (!supabaseClient) return;
    try {
        let req = supabaseClient.from('servicos').select('*');
        if (query) req = req.ilike('nome', `%${query}%`);
        const { data, error } = await req.order('nome');
        if (error) throw error;
        
        const list = document.getElementById('services-list');
        list.innerHTML = '';
        data.forEach(s => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td data-label="Nome">${s.nome}</td>
                <td data-label="Tipo">${s.tipo === 'POR_HORA' ? 'Valor por Hora' : 'Valor Fixo'}</td>
                <td data-label="Valor Base">${formatCurrency(s.valor_base)}</td>
                <td data-label="Ações" class="actions-cell">
                    <button class="action-btn edit" onclick="editService(${s.id})" title="Editar"><i class="fa-solid fa-pen-to-square"></i></button>
                    <button class="action-btn delete" onclick="deleteService(${s.id})" title="Excluir"><i class="fa-solid fa-trash-can"></i></button>
                </td>
            `;
            list.appendChild(tr);
        });
    } catch (e) { console.error(e); }
}

async function saveService(e) {
    e.preventDefault();
    const id = document.getElementById('service-id-field').value;
    const service = {
        nome: document.getElementById('service-name').value.trim(),
        tipo: document.getElementById('service-type').value,
        valor_base: parseFloat(document.getElementById('service-value').value)
    };
    try {
        let res;
        if (id) {
            res = await supabaseClient.from('servicos').update(service).eq('id', id);
        } else {
            res = await supabaseClient.from('servicos').insert([service]);
        }
        if (res.error) throw res.error;
        closeModal('modal-service');
        fetchServices();
    } catch (e) { alert('Erro ao salvar serviço: ' + e.message); }
}

async function editService(id) {
    try {
        const { data, error } = await supabaseClient.from('servicos').select('*').eq('id', id).single();
        if (error) throw error;
        document.getElementById('service-id-field').value = data.id;
        document.getElementById('service-name').value = data.nome;
        document.getElementById('service-type').value = data.tipo;
        document.getElementById('service-value').value = data.valor_base;
        
        document.getElementById('service-modal-title').textContent = 'Editar Serviço';
        document.getElementById('modal-service').classList.remove('hidden');
    } catch (e) { console.error(e); }
}

async function deleteService(id) {
    if (!confirm('Deseja realmente excluir este serviço?')) return;
    try {
        const { error } = await supabaseClient.from('servicos').delete().eq('id', id);
        if (error) throw error;
        fetchServices();
    } catch (e) { alert('Erro ao excluir serviço: ' + e.message); }
}

// 📄 ORÇAMENTOS
async function fetchBudgets(query = '') {
    if (!supabaseClient) return;
    try {
        // Busca orçamento fazendo join com clientes para pegar o nome
        let req = supabaseClient.from('orcamentos').select('*, clientes(nome)');
        const { data, error } = await req.order('id', { ascending: false });
        if (error) throw error;
        
        const list = document.getElementById('budgets-list');
        list.innerHTML = '';
        
        // Filtro manual local para evitar join query complexa no ilike
        const filtered = data.filter(b => {
            if (!query) return true;
            const cNome = b.clientes ? b.clientes.nome : '';
            return cNome.toLowerCase().includes(query.toLowerCase());
        });

        filtered.forEach(b => {
            const tr = document.createElement('tr');
            const dataFmt = new Date(b.data).toLocaleDateString('pt-BR');
            const cNome = b.clientes ? b.clientes.nome : 'Cliente Removido';
            
            let badgeClass = 'badge-orange';
            if (b.status === 'APROVADO') badgeClass = 'badge-green';
            else if (b.status === 'REPROVADO') badgeClass = 'badge-red';
            else if (b.status === 'FINALIZADO') badgeClass = 'badge-blue';

            tr.innerHTML = `
                <td data-label="Nº Orçamento">#${b.id}</td>
                <td data-label="Cliente">${cNome}</td>
                <td data-label="Data">${dataFmt}</td>
                <td data-label="Valor Total">${formatCurrency(b.total)}</td>
                <td data-label="Status"><span class="badge ${badgeClass}">${b.status}</span></td>
                <td data-label="Ações" class="actions-cell">
                    <button class="action-btn edit" onclick="viewBudgetDetails(${b.id})" title="Visualizar/Exportar PDF"><i class="fa-solid fa-file-pdf"></i></button>
                    <button class="action-btn edit" style="color:var(--color-green); background:var(--color-green-glow);" onclick="approveBudget(${b.id})" title="Aprovar/Iniciar O.S."><i class="fa-solid fa-circle-check"></i></button>
                    <button class="action-btn delete" onclick="deleteBudget(${b.id})" title="Excluir"><i class="fa-solid fa-trash-can"></i></button>
                </td>
            `;
            list.appendChild(tr);
        });
    } catch (e) { console.error(e); }
}

async function approveBudget(id) {
    if (!confirm('Deseja aprovar este orçamento e gerar a Ordem de Serviço automaticamente?')) return;
    try {
        // 1. Atualizar status do orçamento para APROVADO
        const { error: updErr } = await supabaseClient.from('orcamentos').update({ status: 'APROVADO' }).eq('id', id);
        if (updErr) throw updErr;

        // 2. Buscar dados do orçamento
        const { data: budget, error: getErr } = await supabaseClient.from('orcamentos').select('*').eq('id', id).single();
        if (getErr) throw getErr;

        // 3. Criar a Ordem de Serviço na tabela `ordem_servico`
        const os = {
            cliente_id: budget.cliente_id,
            total: budget.total,
            status: 'ABERTO'
        };
        const { data: newOS, error: osErr } = await supabaseClient.from('ordem_servico').insert([os]).select().single();
        if (osErr) throw osErr;

        // 4. Copiar itens do orçamento para a ordem de serviço
        const { data: items, error: itemsErr } = await supabaseClient.from('orcamento_itens').select('*').eq('orcamento_id', id);
        if (itemsErr) throw itemsErr;

        const osItems = items.map(item => ({
            ordem_servico_id: newOS.id,
            tipo_item: item.tipo_item,
            produto_id: item.produto_id,
            servico_id: item.servico_id,
            descricao: item.descricao,
            quantidade: item.quantidade,
            valor_unitario: item.valor_unitario,
            valor_total: item.valor_total
        }));
        
        const { error: insItemsErr } = await supabaseClient.from('ordem_servico_itens').insert(osItems);
        if (insItemsErr) throw insItemsErr;

        alert(`Orçamento aprovado! Ordem de Serviço #${newOS.id} gerada.`);
        fetchBudgets();
    } catch (e) {
        alert('Erro ao aprovar orçamento: ' + e.message);
    }
}

async function deleteBudget(id) {
    if (!confirm('Excluir o orçamento apagará todos os itens relacionados. Deseja continuar?')) return;
    try {
        // Exclui itens primeiro (chave estrangeira)
        await supabaseClient.from('orcamento_itens').delete().eq('orcamento_id', id);
        const { error } = await supabaseClient.from('orcamentos').delete().eq('id', id);
        if (error) throw error;
        fetchBudgets();
    } catch (e) { alert('Erro ao excluir orçamento: ' + e.message); }
}

// 📄 DETALHES DO ORÇAMENTO & PDF
async function viewBudgetDetails(id) {
    try {
        const { data: budget, error: bErr } = await supabaseClient.from('orcamentos').select('*, clientes(*)').eq('id', id).single();
        if (bErr) throw bErr;

        const { data: items, error: iErr } = await supabaseClient.from('orcamento_itens').select('*').eq('orcamento_id', id);
        if (iErr) throw iErr;

        const dataFmt = new Date(budget.data).toLocaleDateString('pt-BR');
        const preview = document.getElementById('budget-pdf-preview-content');
        
        let itemsHtml = '';
        items.forEach((item, idx) => {
            const totalItem = formatCurrency(item.valor_total);
            const unitVal = formatCurrency(item.valor_unitario);
            const qtd = parseFloat(item.quantidade).toFixed(2).replace('.', ',');
            itemsHtml += `
                <tr>
                    <td>${idx + 1}</td>
                    <td>${item.descricao}</td>
                    <td>${item.tipo_item}</td>
                    <td>${qtd}</td>
                    <td>${unitVal}</td>
                    <td>${totalItem}</td>
                </tr>
            `;
        });

        preview.innerHTML = `
            <div class="pdf-container" id="pdf-document">
                <div class="pdf-header">
                    <div class="pdf-company-info">
                        <h1>TEC ENERGIA E SOLUÇÕES</h1>
                        <p>CNPJ: 00.000.000/0001-00</p>
                        <p>Telefone: (00) 99999-9999 | Email: contato@empresa.com</p>
                    </div>
                    <div class="pdf-title-info">
                        <h2>ORÇAMENTO</h2>
                        <p style="font-size:14px; font-weight:700; color:#000000;">Nº #${budget.id}</p>
                        <p>Data: ${dataFmt}</p>
                    </div>
                </div>
                
                <div class="pdf-details-grid">
                    <div class="pdf-detail-box">
                        <h3>DADOS DO CLIENTE</h3>
                        <p><strong>Nome:</strong> ${budget.clientes.nome}</p>
                        <p><strong>Telefone:</strong> ${budget.clientes.telefone || '-'}</p>
                        <p><strong>E-mail:</strong> ${budget.clientes.email || '-'}</p>
                        <p><strong>Endereço:</strong> ${budget.clientes.endereco || '-'}</p>
                        <p><strong>Cidade:</strong> ${budget.clientes.cidade || '-'}</p>
                    </div>
                    <div class="pdf-detail-box">
                        <h3>INFORMAÇÕES ADICIONAIS</h3>
                        <p><strong>Status do Orçamento:</strong> ${budget.status}</p>
                        <p><strong>Condição de Pagamento:</strong> A combinar</p>
                        <p><strong>Observações:</strong> ${budget.clientes.observacoes || 'Nenhuma'}</p>
                    </div>
                </div>

                <table class="pdf-table">
                    <thead>
                        <tr>
                            <th style="width: 50px;">Item</th>
                            <th>Descrição</th>
                            <th style="width: 80px;">Tipo</th>
                            <th style="width: 70px;">Qtd</th>
                            <th style="width: 100px;">Unitário</th>
                            <th style="width: 100px;">Total</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${itemsHtml}
                    </tbody>
                </table>

                <div class="pdf-total-box">
                    <span>TOTAL GERAL: ${formatCurrency(budget.total)}</span>
                </div>

                <div class="pdf-signatures">
                    <div class="pdf-signature-line">
                        Representante da Empresa
                    </div>
                    <div class="pdf-signature-line">
                        Aceite do Cliente (Assinatura e Data)
                    </div>
                </div>
            </div>
        `;

        // Atribuir evento de download do PDF para este orçamento específico
        document.getElementById('btn-download-pdf').onclick = () => {
            const opt = {
                margin:       10,
                filename:     `orcamento_${budget.id}_${budget.clientes.nome.replace(/\s+/g, '_')}.pdf`,
                image:        { type: 'jpeg', quality: 0.98 },
                html2canvas:  { scale: 2 },
                jsPDF:        { unit: 'mm', format: 'a4', orientation: 'portrait' }
            };
            const docElement = document.getElementById('pdf-document');
            html2pdf().set(opt).from(docElement).save();
        };

        openModal('modal-budget-view');
    } catch (e) { alert('Erro ao visualizar orçamento: ' + e.message); }
}

// 💼 ORDENS DE SERVIÇO
async function fetchOS(query = '') {
    if (!supabaseClient) return;
    try {
        const { data, error } = await supabaseClient.from('ordem_servico').select('*, clientes(nome)').order('id', { ascending: false });
        if (error) throw error;

        const list = document.getElementById('os-list');
        list.innerHTML = '';

        const filtered = data.filter(os => {
            if (!query) return true;
            const cNome = os.clientes ? os.clientes.nome : '';
            return cNome.toLowerCase().includes(query.toLowerCase());
        });

        filtered.forEach(os => {
            const tr = document.createElement('tr');
            const dataFmt = new Date(os.data).toLocaleDateString('pt-BR');
            const cNome = os.clientes ? os.clientes.nome : 'Cliente Removido';

            let selectHtml = `
                <select onchange="changeOSStatus(${os.id}, this.value)" style="padding: 4px 8px; border-radius:4px; background:rgba(0,0,0,0.2); border:1px solid var(--border-glass); color:#fff; font-family:var(--font-primary);">
                    <option value="ABERTO" ${os.status === 'ABERTO' ? 'selected' : ''}>Aberto</option>
                    <option value="EM ANDAMENTO" ${os.status === 'EM ANDAMENTO' ? 'selected' : ''}>Em Andamento</option>
                    <option value="FINALIZADO" ${os.status === 'FINALIZADO' ? 'selected' : ''}>Finalizado</option>
                    <option value="PAGO" ${os.status === 'PAGO' ? 'selected' : ''}>Pago</option>
                    <option value="CANCELADO" ${os.status === 'CANCELADO' ? 'selected' : ''}>Cancelado</option>
                </select>
            `;

            tr.innerHTML = `
                <td data-label="Nº O.S.">#${os.id}</td>
                <td data-label="Cliente">${cNome}</td>
                <td data-label="Data">${dataFmt}</td>
                <td data-label="Valor Total">${formatCurrency(os.total)}</td>
                <td data-label="Status">${selectHtml}</td>
                <td data-label="Ações" class="actions-cell">
                    <button class="action-btn delete" onclick="deleteOS(${os.id})" title="Excluir"><i class="fa-solid fa-trash-can"></i></button>
                </td>
            `;
            list.appendChild(tr);
        });
    } catch (e) { console.error(e); }
}

async function changeOSStatus(id, newStatus) {
    try {
        const { error } = await supabaseClient.from('ordem_servico').update({ status: newStatus }).eq('id', id);
        if (error) throw error;
        
        // Se mudar para PAGO ou FINALIZADO, podemos automatizar o registro de Vendas
        if (newStatus === 'PAGO') {
            await registrarVendasDaOS(id);
        }
        
        fetchOS();
    } catch (e) { alert('Erro ao alterar status da O.S.: ' + e.message); }
}

// Cria as vendas na tabela `vendas` a partir dos itens da O.S.
async function registrarVendasDaOS(osId) {
    try {
        // Verifica se as vendas desta O.S. já foram registradas
        const { data: exist, error: exErr } = await supabaseClient.from('vendas').select('id').eq('ordem_servico_id', osId);
        if (exErr) throw exErr;
        if (exist && exist.length > 0) return; // Já registradas

        const { data: os, error: osErr } = await supabaseClient.from('ordem_servico').select('*').eq('id', osId).single();
        if (osErr) throw osErr;

        const { data: items, error: iErr } = await supabaseClient.from('ordem_servico_itens').select('*, produtos(*)').eq('ordem_servico_id', osId);
        if (iErr) throw iErr;

        const vendasArr = [];
        for (const item of items) {
            let custoTotal = 0.0;
            if (item.tipo_item === 'PRODUTO' && item.produtos) {
                custoTotal = item.produtos.preco_custo * item.quantidade;
            }
            const lucro = item.valor_total - custoTotal;

            vendasArr.push({
                ordem_servico_id: osId,
                tipo_item: item.tipo_item,
                item_id: item.produto_id || item.servico_id,
                descricao: item.descricao,
                quantidade: item.quantidade,
                valor_total: item.valor_total,
                custo_total: custoTotal,
                lucro: lucro,
                forma_pagamento: 'A COMBINAR' // Pode ser estendido
            });
            
            // Abater estoque dos produtos vendidos
            if (item.tipo_item === 'PRODUTO' && item.produto_id) {
                const novoEstoque = item.produtos.quantidade - item.quantidade;
                await supabaseClient.from('produtos').update({ quantidade: novoEstoque }).eq('id', item.produto_id);
            }
        }

        if (vendasArr.length > 0) {
            const { error: insErr } = await supabaseClient.from('vendas').insert(vendasArr);
            if (insErr) throw insErr;
        }
    } catch (e) {
        console.error('Erro ao registrar vendas da OS:', e);
    }
}

async function deleteOS(id) {
    if (!confirm('Deseja excluir esta Ordem de Serviço? Itens relacionados também serão apagados.')) return;
    try {
        await supabaseClient.from('ordem_servico_itens').delete().eq('ordem_servico_id', id);
        const { error } = await supabaseClient.from('ordem_servico').delete().eq('id', id);
        if (error) throw error;
        fetchOS();
    } catch (e) { alert('Erro ao excluir O.S.: ' + e.message); }
}

// 🧾 DESPESAS
async function fetchExpenses(query = '') {
    if (!supabaseClient) return;
    try {
        let req = supabaseClient.from('despesas').select('*');
        if (query) req = req.ilike('descricao', `%${query}%`);
        const { data, error } = await req.order('id', { ascending: false });
        if (error) throw error;

        const list = document.getElementById('expenses-list');
        list.innerHTML = '';
        data.forEach(d => {
            const tr = document.createElement('tr');
            const dataFmt = new Date(d.data_despesa).toLocaleDateString('pt-BR');
            tr.innerHTML = `
                <td data-label="Data">${dataFmt}</td>
                <td data-label="Descrição">${d.descricao}</td>
                <td data-label="Forma Pagamento">${d.forma_pagamento}</td>
                <td data-label="Valor" style="color:var(--color-red);">${formatCurrency(d.valor)}</td>
                <td data-label="Ações" class="actions-cell">
                    <button class="action-btn delete" onclick="deleteExpense(${d.id})" title="Excluir"><i class="fa-solid fa-trash-can"></i></button>
                </td>
            `;
            list.appendChild(tr);
        });
    } catch (e) { console.error(e); }
}

async function saveExpense(e) {
    e.preventDefault();
    const expense = {
        descricao: document.getElementById('expense-desc').value.trim(),
        valor: parseFloat(document.getElementById('expense-value').value),
        forma_pagamento: document.getElementById('expense-payment').value
    };
    try {
        const { error } = await supabaseClient.from('despesas').insert([expense]);
        if (error) throw error;
        closeModal('modal-expense');
        fetchExpenses();
    } catch (e) { alert('Erro ao salvar despesa: ' + e.message); }
}

async function deleteExpense(id) {
    if (!confirm('Deseja realmente excluir esta despesa?')) return;
    try {
        const { error } = await supabaseClient.from('despesas').delete().eq('id', id);
        if (error) throw error;
        fetchExpenses();
    } catch (e) { alert('Erro ao excluir despesa: ' + e.message); }
}


// ========================================================
// CRIADOR DE ORÇAMENTOS (LÓGICA DO CARRINHO)
// ========================================================
let allClients = [];
let allProducts = [];
let allServices = [];

function setupBudgetCreatorEvents() {
    const btnNew = document.getElementById('btn-new-budget');
    const btnClose = document.getElementById('btn-close-budget-creator');
    const itemType = document.getElementById('budget-item-type');
    const btnAddItem = document.getElementById('btn-add-item-to-list');
    const btnSave = document.getElementById('btn-save-budget');

    if (btnNew) {
        btnNew.addEventListener('click', openBudgetCreator);
    }
    if (btnClose) {
        btnClose.addEventListener('click', closeBudgetCreator);
    }
    if (itemType) {
        itemType.addEventListener('change', populateBudgetCreatorItems);
    }
    if (btnAddItem) {
        btnAddItem.addEventListener('click', addBudgetCartItem);
    }
    if (btnSave) {
        btnSave.addEventListener('click', saveBudget);
    }

    // Auto preencher preço unitário ao mudar seleção de item
    const itemSelect = document.getElementById('budget-item-select');
    if (itemSelect) {
        itemSelect.addEventListener('change', (e) => {
            const itemId = parseInt(e.target.value);
            const type = itemType.value;
            const priceInput = document.getElementById('budget-item-price');
            
            if (type === 'PRODUTO') {
                const prod = allProducts.find(p => p.id === itemId);
                if (prod) priceInput.value = prod.preco_venda;
            } else {
                const serv = allServices.find(s => s.id === itemId);
                if (serv) priceInput.value = serv.valor_base;
            }
        });
    }
}

async function openBudgetCreator() {
    budgetCart = [];
    updateBudgetCartTable();
    
    try {
        // Carrega clientes, produtos e serviços em paralelo
        const [cliRes, prodRes, servRes] = await Promise.all([
            supabaseClient.from('clientes').select('id, nome'),
            supabaseClient.from('produtos').select('*'),
            supabaseClient.from('servicos').select('*')
        ]);

        allClients = cliRes.data || [];
        allProducts = prodRes.data || [];
        allServices = servRes.data || [];

        // Preenche Clientes select
        const cliSelect = document.getElementById('budget-client-select');
        cliSelect.innerHTML = '<option value="">Selecione um cliente...</option>';
        allClients.forEach(c => {
            cliSelect.innerHTML += `<option value="${c.id}">${c.nome}</option>`;
        });

        // Preenche Itens select padrão (Produto)
        document.getElementById('budget-item-type').value = 'PRODUTO';
        populateBudgetCreatorItems();

        document.getElementById('budget-creator-panel').classList.remove('hidden');

    } catch (e) { alert('Erro ao carregar dados para o orçamento: ' + e.message); }
}

function closeBudgetCreator() {
    document.getElementById('budget-creator-panel').classList.add('hidden');
}

function populateBudgetCreatorItems() {
    const type = document.getElementById('budget-item-type').value;
    const itemSelect = document.getElementById('budget-item-select');
    itemSelect.innerHTML = '<option value="">Escolha um item...</option>';

    if (type === 'PRODUTO') {
        allProducts.forEach(p => {
            itemSelect.innerHTML += `<option value="${p.id}">${p.nome} (Estoque: ${parseFloat(p.quantidade).toFixed(1)})</option>`;
        });
    } else {
        allServices.forEach(s => {
            itemSelect.innerHTML += `<option value="${s.id}">${s.nome} (${s.tipo === 'POR_HORA' ? 'Hora' : 'Fixo'})</option>`;
        });
    }
    document.getElementById('budget-item-qty').value = '1';
    document.getElementById('budget-item-price').value = '';
}

function addBudgetCartItem() {
    const select = document.getElementById('budget-item-select');
    const itemId = parseInt(select.value);
    const type = document.getElementById('budget-item-type').value;
    const qty = parseFloat(document.getElementById('budget-item-qty').value);
    const price = parseFloat(document.getElementById('budget-item-price').value);

    if (!itemId || isNaN(qty) || qty <= 0 || isNaN(price) || price < 0) {
        alert('Por favor, preencha todos os campos do item corretamente.');
        return;
    }

    let itemDesc = '';
    if (type === 'PRODUTO') {
        const p = allProducts.find(prod => prod.id === itemId);
        itemDesc = p ? p.nome : '';
    } else {
        const s = allServices.find(serv => serv.id === itemId);
        itemDesc = s ? s.nome : '';
    }

    // Adiciona ao carrinho temporário
    budgetCart.push({
        id: Date.now(), // ID provisório para remoção
        tipo_item: type,
        produto_id: type === 'PRODUTO' ? itemId : null,
        servico_id: type === 'SERVICO' ? itemId : null,
        descricao: itemDesc,
        quantidade: qty,
        valor_unitario: price,
        valor_total: qty * price
    });

    updateBudgetCartTable();

    // Reset campos de item
    select.value = '';
    document.getElementById('budget-item-qty').value = '1';
    document.getElementById('budget-item-price').value = '';
}

function removeBudgetCartItem(tempId) {
    budgetCart = budgetCart.filter(item => item.id !== tempId);
    updateBudgetCartTable();
}

function updateBudgetCartTable() {
    const list = document.getElementById('budget-items-list');
    list.innerHTML = '';
    
    let total = 0.0;
    
    if (budgetCart.length === 0) {
        document.getElementById('cart-empty').classList.remove('hidden');
    } else {
        document.getElementById('cart-empty').classList.add('hidden');
    }

    budgetCart.forEach(item => {
        total += item.valor_total;
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>${item.descricao}</td>
            <td><span class="badge ${item.tipo_item === 'PRODUTO' ? 'badge-blue' : 'badge-purple'}">${item.tipo_item}</span></td>
            <td>${parseFloat(item.quantidade).toFixed(2).replace('.', ',')}</td>
            <td>${formatCurrency(item.valor_unitario)}</td>
            <td>${formatCurrency(item.valor_total)}</td>
            <td>
                <button type="button" class="action-btn delete" onclick="removeBudgetCartItem(${item.id})">
                    <i class="fa-solid fa-trash-can"></i>
                </button>
            </td>
        `;
        list.appendChild(tr);
    });

    document.getElementById('summary-items-count').textContent = budgetCart.length;
    document.getElementById('summary-total-value').textContent = formatCurrency(total);
}

async function saveBudget() {
    const clientVal = document.getElementById('budget-client-select').value;
    if (!clientVal) {
        alert('Por favor, selecione um cliente.');
        return;
    }
    if (budgetCart.length === 0) {
        alert('Adicione pelo menos um item ao orçamento.');
        return;
    }

    const clientId = parseInt(clientVal);
    const totalGeral = budgetCart.reduce((sum, item) => sum + item.valor_total, 0);

    const budget = {
        cliente_id: clientId,
        total: totalGeral,
        status: 'ABERTO'
    };

    try {
        // 1. Inserir orçamento cabeçalho
        const { data: newBudget, error: bErr } = await supabaseClient.from('orcamentos').insert([budget]).select().single();
        if (bErr) throw bErr;

        // 2. Formatar itens com o ID do orçamento gerado
        const itemsToInsert = budgetCart.map(item => ({
            orcamento_id: newBudget.id,
            tipo_item: item.tipo_item,
            produto_id: item.produto_id,
            servico_id: item.servico_id,
            descricao: item.descricao,
            quantidade: item.quantidade,
            valor_unitario: item.valor_unitario,
            valor_total: item.valor_total
        }));

        // 3. Inserir itens
        const { error: iErr } = await supabaseClient.from('orcamento_itens').insert(itemsToInsert);
        if (iErr) throw iErr;

        alert(`Orçamento #${newBudget.id} salvo com sucesso!`);
        closeBudgetCreator();
        switchView('budgets');

    } catch (e) {
        alert('Erro ao salvar orçamento completo: ' + e.message);
    }
}


// ========================================================
// 📊 DASHBOARD & GRÁFICOS (CHART.JS)
// ========================================================
async function fetchDashboard() {
    if (!supabaseClient) return;
    try {
        // Buscas do banco de dados para os KPIs em tempo real
        const [vendasRes, servicosRes, clientesRes, despesasRes] = await Promise.all([
            supabaseClient.from('vendas').select('*'),
            supabaseClient.from('servicos_realizados').select('*'),
            supabaseClient.from('clientes').select('id', { count: 'exact' }),
            supabaseClient.from('despesas').select('*')
        ]);

        const vendas = vendasRes.data || [];
        const servicos = servicosRes.data || [];
        const clientesCount = clientesRes.count || 0;
        const despesas = despesasRes.data || [];

        // 1. Faturamento e Lucro Diários (Hoje)
        const hoje = new Date().toLocaleDateString('en-CA'); // yyyy-mm-dd
        
        let fatDiario = 0.0;
        let lucDiario = 0.0;

        vendas.forEach(v => {
            const dataV = new Date(v.data_venda).toLocaleDateString('en-CA');
            if (dataV === hoje) {
                fatDiario += v.valor_total;
                lucDiario += v.lucro;
            }
        });
        servicos.forEach(s => {
            const dataS = new Date(s.data_servico).toLocaleDateString('en-CA');
            if (dataS === hoje) {
                fatDiario += s.valor;
                lucDiario += s.valor; // 100% lucro operacional no serviço
            }
        });

        // 2. Faturamento e Lucro Mensal (Mês Atual)
        const anoAtual = new Date().getFullYear();
        const mesAtual = new Date().getMonth(); // 0 a 11

        let fatMensal = 0.0;
        let lucMensal = 0.0;

        vendas.forEach(v => {
            const date = new Date(v.data_venda);
            if (date.getFullYear() === anoAtual && date.getMonth() === mesAtual) {
                fatMensal += v.valor_total;
                lucMensal += v.lucro;
            }
        });
        servicos.forEach(s => {
            const date = new Date(s.data_servico);
            if (date.getFullYear() === anoAtual && date.getMonth() === mesAtual) {
                fatMensal += s.valor;
                lucMensal += s.valor;
            }
        });
        
        // Abater despesas do mês atual no Lucro Mensal
        let despMensal = 0.0;
        despesas.forEach(d => {
            const date = new Date(d.data_despesa);
            if (date.getFullYear() === anoAtual && date.getMonth() === mesAtual) {
                despMensal += d.valor;
            }
        });
        lucMensal -= despMensal;

        // Renderizar KPIs na tela
        document.getElementById('kpi-fat-diario').textContent = formatCurrency(fatDiario);
        document.getElementById('kpi-fat-mensal').textContent = formatCurrency(fatMensal);
        document.getElementById('kpi-luc-mensal').textContent = formatCurrency(lucMensal);
        document.getElementById('kpi-clientes').textContent = clientesCount;

        // 3. Gerar Gráfico de Linha Diário do Mês
        const diasNoMes = new Date(anoAtual, mesAtual + 1, 0).getDate();
        const diasLabels = [];
        const faturamentosPorDia = [];
        
        for (let i = 1; i <= diasNoMes; i++) {
            diasLabels.push(i);
            faturamentosPorDia.push(0.0);
        }

        vendas.forEach(v => {
            const date = new Date(v.data_venda);
            if (date.getFullYear() === anoAtual && date.getMonth() === mesAtual) {
                const dia = date.getDate();
                faturamentosPorDia[dia - 1] += v.valor_total;
            }
        });
        servicos.forEach(s => {
            const date = new Date(s.data_servico);
            if (date.getFullYear() === anoAtual && date.getMonth() === mesAtual) {
                const dia = date.getDate();
                faturamentosPorDia[dia - 1] += s.valor;
            }
        });

        renderSalesChart(diasLabels, faturamentosPorDia);

    } catch (e) { console.error('Erro dashboard:', e); }
}

function renderSalesChart(labels, data) {
    const ctx = document.getElementById('salesChart').getContext('2d');
    
    if (salesChart) {
        salesChart.destroy();
    }

    salesChart = new Chart(ctx, {
        type: 'line',
        data: {
            labels: labels,
            datasets: [{
                label: 'Faturamento (R$)',
                data: data,
                borderColor: '#ffffff',
                backgroundColor: 'rgba(255, 255, 255, 0.08)',
                borderWidth: 3,
                fill: true,
                tension: 0.35,
                pointRadius: 4,
                pointBackgroundColor: '#ffffff',
                pointBorderColor: '#000000'
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { display: false }
            },
            scales: {
                x: {
                    grid: { color: 'rgba(255, 255, 255, 0.03)' },
                    ticks: { color: '#94a3b8', font: { family: 'Outfit' } }
                },
                y: {
                    grid: { color: 'rgba(255, 255, 255, 0.03)' },
                    ticks: {
                        color: '#94a3b8',
                        font: { family: 'Outfit' },
                        callback: function(value) { return 'R$ ' + value; }
                    }
                }
            }
        }
    });
}

// Utilitário de formatação de moeda
function formatCurrency(value) {
    return new Intl.NumberFormat('pt-BR', {
        style: 'currency',
        currency: 'BRL'
    }).format(value || 0);
}

// Registro do Service Worker para PWA
if ('serviceWorker' in navigator) {
    window.addEventListener('load', () => {
        navigator.serviceWorker.register('sw.js')
            .then(reg => console.log('Service Worker registrado com sucesso:', reg.scope))
            .catch(err => console.error('Erro ao registrar o Service Worker:', err));
    });
}
