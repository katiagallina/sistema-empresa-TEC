// Estado da Aplicação
let supabaseClient = null;
let currentSession = null;
let currentView = 'dashboard';
let salesChart = null;

// Lógica de Calendário e Caixa
let currentCalendarDate = new Date();
let allMonthEntries = [];
let allMonthExpenses = [];
let currentCaixaDate = new Date();
let selectedDayEvents = [];
let selectedDayDateStr = '';

// Itens temporários do orçamento sendo criado (Carrinho)
let budgetCart = [];
let currentEditingBudgetId = null;

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
    
    // Evento de submit para Entrada do Caixa
    const entryForm = document.getElementById('entry-form');
    if (entryForm) {
        entryForm.addEventListener('submit', saveEntry);
    }
    
    // Navegação do Calendário
    const btnPrevMonth = document.getElementById('btn-prev-month');
    const btnNextMonth = document.getElementById('btn-next-month');
    if (btnPrevMonth) btnPrevMonth.addEventListener('click', () => changeCalendarMonth(-1));
    if (btnNextMonth) btnNextMonth.addEventListener('click', () => changeCalendarMonth(1));
    setupAgendaReminderEvents();
    
    // Inicializar lógica de parcelas de boleto
    setupInstallmentsLogic();
    
    // Botões de adição rápida no modal de detalhes do dia
    const btnDayAddEntrada = document.getElementById('btn-day-add-entrada');
    const btnDayAddSaida = document.getElementById('btn-day-add-saida');
    if (btnDayAddEntrada) {
        btnDayAddEntrada.addEventListener('click', () => {
            closeModal('modal-day-details');
            openModal('modal-entry');
            if (selectedDayDateStr) {
                document.getElementById('entry-date').value = selectedDayDateStr;
            }
        });
    }
    if (btnDayAddSaida) {
        btnDayAddSaida.addEventListener('click', () => {
            closeModal('modal-day-details');
            openModal('modal-expense');
            if (selectedDayDateStr) {
                document.getElementById('expense-date').value = selectedDayDateStr;
            }
        });
    }

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
        { inputId: 'caixa-search', fetchFn: () => renderCaixaTables() }
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

function setupInstallmentsLogic() {
    const entryPayment = document.getElementById('entry-payment');
    const installmentsGroup = document.getElementById('entry-installments-group');
    const installmentsInput = document.getElementById('entry-installments');
    const installmentDatesContainer = document.getElementById('entry-installments-dates-container');
    const entryDate = document.getElementById('entry-date');

    if (!entryPayment || !installmentsGroup || !installmentsInput || !installmentDatesContainer || !entryDate) return;

    function updateInstallmentFields() {
        const payment = entryPayment.value;
        const numInstallments = parseInt(installmentsInput.value) || 1;
        const idFieldVal = document.getElementById('entry-id-field').value;

        // Se estiver editando, não permitir parcelamento
        if (idFieldVal) {
            installmentsGroup.style.display = 'none';
            installmentDatesContainer.style.display = 'none';
            return;
        }

        if (payment === 'BOLETO') {
            installmentsGroup.style.display = 'block';
            if (numInstallments > 1) {
                installmentDatesContainer.innerHTML = '';
                installmentDatesContainer.style.display = 'block';
                
                const baseDateVal = entryDate.value;
                let baseDate = baseDateVal ? new Date(baseDateVal + 'T12:00:00') : new Date();
                
                for (let i = 1; i <= numInstallments; i++) {
                    let dueDate = new Date(baseDate.getTime());
                    dueDate.setMonth(baseDate.getMonth() + (i - 1));
                    const dateStr = dueDate.toLocaleDateString('en-CA');
                    
                    const div = document.createElement('div');
                    div.className = 'input-group';
                    div.style.marginTop = '10px';
                    div.innerHTML = `
                        <label>Vencimento da Parcela ${i}/${numInstallments} *</label>
                        <input type="date" class="entry-installment-date" data-index="${i}" value="${dateStr}" required>
                    `;
                    installmentDatesContainer.appendChild(div);
                }
            } else {
                installmentDatesContainer.innerHTML = '';
                installmentDatesContainer.style.display = 'none';
            }
        } else {
            installmentsGroup.style.display = 'none';
            installmentDatesContainer.innerHTML = '';
            installmentDatesContainer.style.display = 'none';
        }
    }

    entryPayment.addEventListener('change', () => {
        if (entryPayment.value !== 'BOLETO') {
            installmentsInput.value = 1;
        }
        updateInstallmentFields();
    });

    installmentsInput.addEventListener('input', updateInstallmentFields);
    entryDate.addEventListener('change', updateInstallmentFields);
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
        'dashboard': 'Calendário',
        'clients': 'Clientes',
        'products': 'Produtos',
        'services': 'Serviços',
        'budgets': 'Orçamentos',
        'os': 'Ordens de Serviço',
        'expenses': 'Caixa'
    };
    document.getElementById('current-view-title').textContent = viewName === 'dashboard' ? 'Agenda' : (viewTitles[viewName] || 'Sistema');

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
    if (viewName === 'dashboard') fetchCalendar();
    else if (viewName === 'clients') fetchClients();
    else if (viewName === 'products') fetchProducts();
    else if (viewName === 'services') fetchServices();
    else if (viewName === 'budgets') fetchBudgets();
    else if (viewName === 'os') fetchOS();
    else if (viewName === 'expenses') fetchCaixa();
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
    } else if (modalId === 'modal-entry') {
        document.getElementById('entry-form').reset();
        document.getElementById('entry-id-field').value = '';
        document.getElementById('entry-modal-title').textContent = 'Novo Lançamento de Entrada';
        document.getElementById('entry-date').value = new Date().toLocaleDateString('en-CA');
    } else if (modalId === 'modal-expense') {
        document.getElementById('expense-form').reset();
        document.getElementById('expense-id-field').value = '';
        document.getElementById('expense-modal-title').textContent = 'Nova Despesa / Saída';
        document.getElementById('expense-date').value = new Date().toLocaleDateString('en-CA');
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
    const msg = id ? 'Deseja realmente salvar as alterações deste cliente?' : 'Deseja realmente cadastrar este cliente?';
    if (!confirm(msg)) return;

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
    if (!confirm('Deseja realmente editar este cliente?')) return;
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
    const msg = id ? 'Deseja realmente salvar as alterações deste produto?' : 'Deseja realmente cadastrar este produto?';
    if (!confirm(msg)) return;

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
    if (!confirm('Deseja realmente editar este produto?')) return;
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
    const msg = id ? 'Deseja realmente salvar as alterações deste serviço?' : 'Deseja realmente cadastrar este serviço?';
    if (!confirm(msg)) return;

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
    if (!confirm('Deseja realmente editar este serviço?')) return;
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
                    <button class="action-btn edit" onclick="openBudgetCreator(${b.id})" title="Editar Orçamento"><i class="fa-solid fa-pen-to-square"></i></button>
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
        // Resetar o título do modal para Orçamento
        const titleEl = document.querySelector('#modal-budget-view .modal-header h2');
        if (titleEl) titleEl.textContent = 'Visualizar Orçamento';

        const { data: budget, error: bErr } = await supabaseClient.from('orcamentos').select('*, clientes(*)').eq('id', id).single();
        if (bErr) throw bErr;

        const { data: items, error: iErr } = await supabaseClient.from('orcamento_itens').select('*').eq('orcamento_id', id);
        if (iErr) throw iErr;

        const dataFmt = new Date(budget.data).toLocaleDateString('pt-BR');
        const preview = document.getElementById('budget-pdf-preview-content');
        
        let itemsHtml = '';
        let valorMaoDeObra = 0;

        items.forEach((item) => {
            if (item.tipo_item === 'SERVICO') {
                valorMaoDeObra += parseFloat(item.valor_total);
            } else {
                const totalItem = formatCurrency(item.valor_total);
                const unitVal = formatCurrency(item.valor_unitario);
                const qtyVal = parseFloat(item.quantidade);
                const qtd = Number.isInteger(qtyVal) ? qtyVal.toString() : qtyVal.toFixed(2).replace('.', ',');
                
                itemsHtml += `
                    <tr>
                        <td class="pdf-table-qty">${qtd}</td>
                        <td>${item.descricao.toUpperCase()}</td>
                        <td class="pdf-table-center">${unitVal}</td>
                        <td class="pdf-table-center">${totalItem}</td>
                    </tr>
                `;
            }
        });

        // Adicionar linha de Mão de Obra se houver
        if (valorMaoDeObra > 0) {
            itemsHtml += `
                <tr>
                    <td></td>
                    <td style="font-weight: bold;">MÃO DE OBRA E DESLOCAMENTO</td>
                    <td></td>
                    <td class="pdf-table-center" style="font-weight: bold;">${formatCurrency(valorMaoDeObra)}</td>
                </tr>
            `;
        }

        // Linha de Total Geral
        itemsHtml += `
            <tr style="font-weight: bold;">
                <td></td>
                <td>TOTAL</td>
                <td></td>
                <td class="pdf-table-center" style="font-weight: bold;">${formatCurrency(budget.total)}</td>
            </tr>
        `;

        const dateOptions = { year: 'numeric', month: 'long', day: 'numeric' };
        const currentDateFormatted = new Date().toLocaleDateString('pt-BR', dateOptions);
        const cityDateStr = `Getúlio Vargas, ${currentDateFormatted}`;

        preview.innerHTML = `
            <div class="pdf-container" id="pdf-document">
                <div class="pdf-header">
                    <div class="pdf-header-top">
                        <div class="pdf-header-logo-container">
                            <img src="logo.png" alt="TEC Energia e Soluções" class="pdf-header-logo">
                        </div>
                        <div class="pdf-header-company-info">
                            <h1 class="pdf-company-title">TEC ENERGIA E SOLUÇÕES</h1>
                            <p class="pdf-company-subtitle">ENTREGANDO SERIEDADE E QUALIDADE</p>
                            <p class="pdf-company-cnpj">CNPJ 59.241.256.0001-33</p>
                        </div>
                    </div>
                    <div class="pdf-header-bottom">
                        <p>Endereço: Rua Batista Guidi, 795 Bairro Santa Catarina</p>
                        <p>Getúlio Vargas - RS</p>
                        <p class="pdf-company-contacts">CONTATOS: TIAGO 991838023 &nbsp;&nbsp;&nbsp;&nbsp; EMERSON 991825194</p>
                    </div>
                </div>
                
                <div class="pdf-client-info-row">
                    <span>NOME: ${budget.clientes.nome.toUpperCase()}</span>
                </div>

                <table class="pdf-table">
                    <thead>
                        <tr>
                            <th style="width: 110px; text-align: center;">QUANTIDADE</th>
                            <th style="text-align: center;">PRODUTO</th>
                            <th style="width: 120px; text-align: center;">VALOR UNIT.</th>
                            <th style="width: 120px; text-align: center;">VALOR TOTAL</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${itemsHtml}
                    </tbody>
                </table>

                <div class="pdf-footer-section">
                    <div class="pdf-validity-info">
                        ORÇAMENTO VÁLIDO POR 30 DIAS
                    </div>

                    <div class="pdf-current-date-info">
                        ${cityDateStr}
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
                    <button class="action-btn edit" onclick="viewOSDetails(${os.id})" title="Visualizar/Exportar PDF"><i class="fa-solid fa-file-pdf"></i></button>
                    <button class="action-btn delete" onclick="deleteOS(${os.id})" title="Excluir"><i class="fa-solid fa-trash-can"></i></button>
                </td>
            `;
            list.appendChild(tr);
        });
    } catch (e) { console.error(e); }
}

async function changeOSStatus(id, newStatus) {
    if (!confirm(`Deseja realmente alterar o status da O.S. para ${newStatus}?`)) {
        fetchOS(); // Reverte a seleção no dropdown
        return;
    }
    try {
        let paymentMethod = 'A COMBINAR';
        if (newStatus === 'PAGO' || newStatus === 'FINALIZADO') {
            const inputPay = prompt('O.S. finalizada/paga. Qual foi a forma de pagamento?\nOpções: PIX, DINHEIRO, BOLETO, CARTÃO, CHEQUE', 'PIX');
            if (inputPay === null) {
                fetchOS(); // Reverte a seleção no dropdown
                return;
            }
            paymentMethod = inputPay.trim() ? inputPay.trim() : 'PIX';
        }

        const { error } = await supabaseClient.from('ordem_servico').update({ status: newStatus }).eq('id', id);
        if (error) throw error;
        
        // Se mudar para PAGO ou FINALIZADO, automatizar o registro de Vendas
        if (newStatus === 'PAGO' || newStatus === 'FINALIZADO') {
            await registrarVendasDaOS(id, paymentMethod);
        }
        
        fetchOS();
    } catch (e) { alert('Erro ao alterar status da O.S.: ' + e.message); }
}

// Cria as vendas na tabela `vendas` a partir dos itens da O.S.
async function registrarVendasDaOS(osId, formaPagamento = 'A COMBINAR') {
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
                forma_pagamento: formaPagamento.toUpperCase()
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

// 📄 DETALHES DA ORDEM DE SERVIÇO & PDF
async function viewOSDetails(id) {
    try {
        const { data: os, error: osErr } = await supabaseClient.from('ordem_servico').select('*, clientes(*)').eq('id', id).single();
        if (osErr) throw osErr;

        const { data: items, error: iErr } = await supabaseClient.from('ordem_servico_itens').select('*').eq('ordem_servico_id', id);
        if (iErr) throw iErr;

        const dataFmt = new Date(os.data).toLocaleDateString('pt-BR');
        const preview = document.getElementById('budget-pdf-preview-content');
        
        let itemsHtml = '';
        let valorMaoDeObra = 0;

        items.forEach((item) => {
            if (item.tipo_item === 'SERVICO') {
                valorMaoDeObra += parseFloat(item.valor_total);
            } else {
                const totalItem = formatCurrency(item.valor_total);
                const unitVal = formatCurrency(item.valor_unitario);
                const qtyVal = parseFloat(item.quantidade);
                const qtd = Number.isInteger(qtyVal) ? qtyVal.toString() : qtyVal.toFixed(2).replace('.', ',');
                
                itemsHtml += `
                    <tr>
                        <td class="pdf-table-qty">${qtd}</td>
                        <td>${item.descricao.toUpperCase()}</td>
                        <td class="pdf-table-center">${unitVal}</td>
                        <td class="pdf-table-center">${totalItem}</td>
                    </tr>
                `;
            }
        });

        // Adicionar linha de Mão de Obra se houver
        if (valorMaoDeObra > 0) {
            itemsHtml += `
                <tr>
                    <td></td>
                    <td style="font-weight: bold;">MÃO DE OBRA E DESLOCAMENTO</td>
                    <td></td>
                    <td class="pdf-table-center" style="font-weight: bold;">${formatCurrency(valorMaoDeObra)}</td>
                </tr>
            `;
        }

        // Linha de Total Geral
        itemsHtml += `
            <tr style="font-weight: bold;">
                <td></td>
                <td>TOTAL</td>
                <td></td>
                <td class="pdf-table-center" style="font-weight: bold;">${formatCurrency(os.total)}</td>
            </tr>
        `;

        const dateOptions = { year: 'numeric', month: 'long', day: 'numeric' };
        const currentDateFormatted = new Date().toLocaleDateString('pt-BR', dateOptions);
        const cityDateStr = `Getúlio Vargas, ${currentDateFormatted}`;

        // Mudar o título do Modal para O.S.
        const modalTitle = document.querySelector('#modal-budget-view .modal-header h2');
        if (modalTitle) modalTitle.textContent = 'Visualizar Ordem de Serviço';

        preview.innerHTML = `
            <div class="pdf-container" id="pdf-document">
                <div class="pdf-header">
                    <div class="pdf-header-top">
                        <div class="pdf-header-logo-container">
                            <img src="logo.png" alt="TEC Energia e Soluções" class="pdf-header-logo">
                        </div>
                        <div class="pdf-header-company-info">
                            <h1 class="pdf-company-title">TEC ENERGIA E SOLUÇÕES</h1>
                            <p class="pdf-company-subtitle">ENTREGANDO SERIEDADE E QUALIDADE</p>
                            <p class="pdf-company-cnpj">CNPJ 59.241.256.0001-33</p>
                        </div>
                    </div>
                    <div class="pdf-header-bottom">
                        <p>Endereço: Rua Batista Guidi, 795 Bairro Santa Catarina</p>
                        <p>Getúlio Vargas - RS</p>
                        <p class="pdf-company-contacts">CONTATOS: TIAGO 991838023 &nbsp;&nbsp;&nbsp;&nbsp; EMERSON 991825194</p>
                    </div>
                </div>
                
                <div style="display: flex; justify-content: space-between; align-items: center; margin-top: 15px; margin-bottom: 10px;">
                    <div class="pdf-client-info-row" style="margin: 0;">
                        NOME: ${os.clientes.nome.toUpperCase()}
                    </div>
                    <div style="font-size: 13px; font-weight: bold; color: #000000; text-transform: uppercase;">
                        ORDEM DE SERVIÇO Nº #${os.id}
                    </div>
                </div>

                <table class="pdf-table">
                    <thead>
                        <tr>
                            <th style="width: 110px; text-align: center;">QUANTIDADE</th>
                            <th style="text-align: center;">PRODUTO / SERVIÇO</th>
                            <th style="width: 120px; text-align: center;">VALOR UNIT.</th>
                            <th style="width: 120px; text-align: center;">VALOR TOTAL</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${itemsHtml}
                    </tbody>
                </table>

                <div class="pdf-footer-section">
                    <div class="pdf-signatures" style="display: grid; grid-template-columns: 1fr 1fr; gap: 40px; text-align: center; margin-top: 20px; margin-bottom: 20px;">
                        <div class="pdf-signature-line" style="border-top: 1px solid #000; padding-top: 8px; font-size: 10px; color: #000; font-weight: bold;">
                            ASSINATURA DO TÉCNICO
                        </div>
                        <div class="pdf-signature-line" style="border-top: 1px solid #000; padding-top: 8px; font-size: 10px; color: #000; font-weight: bold;">
                            ASSINATURA DO CLIENTE
                        </div>
                    </div>

                    <div class="pdf-current-date-info" style="margin-bottom: 10px;">
                        ${cityDateStr}
                    </div>
                </div>
            </div>
        `;

        // Atribuir evento de download do PDF
        document.getElementById('btn-download-pdf').onclick = () => {
            const opt = {
                margin:       10,
                filename:     `ordem_servico_${os.id}_${os.clientes.nome.replace(/\s+/g, '_')}.pdf`,
                image:        { type: 'jpeg', quality: 0.98 },
                html2canvas:  { scale: 2 },
                jsPDF:        { unit: 'mm', format: 'a4', orientation: 'portrait' }
            };
            const docElement = document.getElementById('pdf-document');
            html2pdf().set(opt).from(docElement).save();
        };

        // Resetar o título do modal ao fechar
        const modal = document.getElementById('modal-budget-view');
        const originalClose = modal.querySelector('.modal-close-btn');
        if (originalClose) {
            originalClose.onclick = () => {
                closeModal('modal-budget-view');
                const titleEl = document.querySelector('#modal-budget-view .modal-header h2');
                if (titleEl) titleEl.textContent = 'Visualizar Orçamento';
            };
        }

        openModal('modal-budget-view');
    } catch (e) { alert('Erro ao visualizar Ordem de Serviço: ' + e.message); }
}

// ========================================================
// 📅 LÓGICA DO CALENDÁRIO
// ========================================================
async function fetchCalendar() {
    if (!supabaseClient) return;
    try {
        const year = currentCalendarDate.getFullYear();
        const month = currentCalendarDate.getMonth();
        const numDays = new Date(year, month + 1, 0).getDate();
        
        const startDate = new Date(year, month, 1).toISOString();
        const endDate = new Date(year, month, numDays, 23, 59, 59).toISOString();
        
        // Buscar em paralelo as entradas (servicos_realizados) e ordens de servico
        const [servicesRes, osRes] = await Promise.all([
            supabaseClient.from('servicos_realizados').select('*, clientes(nome)').gte('data_servico', startDate).lte('data_servico', endDate),
            supabaseClient.from('ordem_servico').select('*, clientes(nome)').gte('data', startDate).lte('data', endDate)
        ]);
        
        renderCalendar(servicesRes.data || [], osRes.data || []);
    } catch (e) {
        console.error('Erro ao buscar calendário:', e);
    }
}

function renderCalendar(completedServices, ordensServico) {
    const grid = document.getElementById('calendar-days-grid');
    if (!grid) return;
    
    // Atualizar título
    const monthYearTitle = currentCalendarDate.toLocaleDateString('pt-BR', { month: 'long', year: 'numeric' });
    document.getElementById('calendar-month-year-title').textContent = monthYearTitle;
    
    grid.innerHTML = '';
    
    // Dias da semana
    const weekDays = ['Dom', 'Seg', 'Ter', 'Qua', 'Qui', 'Sex', 'Sáb'];
    weekDays.forEach(day => {
        const div = document.createElement('div');
        div.className = 'calendar-day-header';
        div.textContent = day;
        grid.appendChild(div);
    });
    
    const year = currentCalendarDate.getFullYear();
    const month = currentCalendarDate.getMonth();
    
    const firstDayIndex = new Date(year, month, 1).getDay();
    const numDays = new Date(year, month + 1, 0).getDate();
    const prevLastDay = new Date(year, month, 0).getDate();
    
    // Agrupar eventos
    const eventsByDay = {};
    for (let i = 1; i <= numDays; i++) {
        eventsByDay[i] = [];
    }
    
    completedServices.forEach(s => {
        const day = new Date(s.data_servico).getDate();
        if (eventsByDay[day]) {
            eventsByDay[day].push({ type: 'completed', data: s });
        }
    });
    
    ordensServico.forEach(os => {
        const day = new Date(os.data).getDate();
        if (eventsByDay[day]) {
            eventsByDay[day].push({ type: 'os', data: os });
        }
    });
    
    // 1. Dias do mês anterior
    for (let x = firstDayIndex; x > 0; x--) {
        const dayNum = prevLastDay - x + 1;
        const cell = document.createElement('div');
        cell.className = 'calendar-day other-month';
        cell.innerHTML = `<span class="calendar-day-number">${dayNum}</span>`;
        grid.appendChild(cell);
    }
    
    // 2. Dias do mês atual
    const today = new Date();
    const isCurrentMonth = today.getFullYear() === year && today.getMonth() === month;
    
    for (let i = 1; i <= numDays; i++) {
        const cell = document.createElement('div');
        cell.className = 'calendar-day';
        if (isCurrentMonth && today.getDate() === i) {
            cell.classList.add('today');
        }
        
        let eventsHtml = '';
        const dayEvents = eventsByDay[i] || [];
        
        // Mostrar até 2 badges e depois um "+X mais"
        const maxVisible = 2;
        dayEvents.slice(0, maxVisible).forEach(evt => {
            if (evt.type === 'completed') {
                const desc = evt.data.descricao_servico;
                eventsHtml += `<span class="calendar-badge badge-completed" title="${desc}">${desc}</span>`;
            } else {
                const cNome = evt.data.clientes ? evt.data.clientes.nome : 'Cliente';
                eventsHtml += `<span class="calendar-badge badge-os" title="OS #${evt.data.id} - ${cNome}">OS #${evt.data.id} - ${cNome}</span>`;
            }
        });
        
        if (dayEvents.length > maxVisible) {
            eventsHtml += `<span class="calendar-badge badge-more">+${dayEvents.length - maxVisible} mais</span>`;
        }
        
        cell.innerHTML = `
            <span class="calendar-day-number">${i}</span>
            <div class="calendar-events">${eventsHtml}</div>
        `;
        
        const dayStr = `${year}-${String(month + 1).padStart(2, '0')}-${String(i).padStart(2, '0')}`;
        cell.addEventListener('click', () => openDayDetailsModal(dayStr, dayEvents));
        
        grid.appendChild(cell);
    }
    
    // 3. Dias do mês seguinte
    const totalCells = firstDayIndex + numDays;
    const remaining = 42 - totalCells;
    for (let i = 1; i <= remaining; i++) {
        const cell = document.createElement('div');
        cell.className = 'calendar-day other-month';
        cell.innerHTML = `<span class="calendar-day-number">${i}</span>`;
        grid.appendChild(cell);
    }
}

function changeCalendarMonth(offset) {
    currentCalendarDate.setMonth(currentCalendarDate.getMonth() + offset);
    fetchCalendar();
}

function changeCaixaMonth(offset) {
    currentCaixaDate.setMonth(currentCaixaDate.getMonth() + offset);
    fetchCaixa();
}

async function openDayDetailsModal(dateString, dayEvents) {
    selectedDayDateStr = dateString;
    selectedDayEvents = dayEvents;
    
    const dateParts = dateString.split('-');
    const dateFmt = `${dateParts[2]}/${dateParts[1]}/${dateParts[0]}`;
    document.getElementById('day-details-title').textContent = `Serviços do Dia - ${dateFmt}`;
    
    // Buscar também as despesas/saídas desse dia específico
    let dayExpenses = [];
    try {
        const startDay = `${dateString}T00:00:00`;
        const endDay = `${dateString}T23:59:59`;
        const { data } = await supabaseClient.from('despesas').select('*').gte('data_despesa', startDay).lte('data_despesa', endDay);
        dayExpenses = data || [];
    } catch (e) {
        console.error(e);
    }
    
    // Calcular balanço rápido
    let sumEntradas = 0;
    let sumSaidas = 0;
    
    dayEvents.forEach(evt => {
        if (evt.type === 'completed') {
            sumEntradas += evt.data.valor;
        }
    });
    dayExpenses.forEach(exp => {
        sumSaidas += exp.valor;
    });
    
    document.getElementById('day-summary-entradas').textContent = formatCurrency(sumEntradas);
    document.getElementById('day-summary-saidas').textContent = formatCurrency(sumSaidas);
    
    const totalDayBalance = sumEntradas - sumSaidas;
    const balanceElem = document.getElementById('day-summary-lucro');
    balanceElem.textContent = formatCurrency(totalDayBalance);
    balanceElem.classList.remove('green', 'red');
    if (totalDayBalance > 0) balanceElem.classList.add('green');
    else if (totalDayBalance < 0) balanceElem.classList.add('red');
    
    // Renderizar lista de atividades
    const container = document.getElementById('day-events-list-container');
    container.innerHTML = '';
    
    // Adicionar entradas e OS do array local
    dayEvents.forEach(evt => {
        const item = document.createElement('div');
        item.className = 'day-event-item';
        
        if (evt.type === 'completed') {
            const clientName = evt.data.clientes ? evt.data.clientes.nome : 'Sem Cliente';
            item.innerHTML = `
                <div class="day-event-info">
                    <span class="day-event-title"><i class="fa-solid fa-arrow-up-right-from-square" style="color: #10b981; margin-right: 6px;"></i> ${evt.data.descricao_servico}</span>
                    <span class="day-event-sub">Cliente: ${clientName} | Pagamento: ${evt.data.forma_pagamento}</span>
                </div>
                <div style="display: flex; align-items: center; gap: 12px;">
                    <span class="day-event-amount positive">+${formatCurrency(evt.data.valor)}</span>
                    <button class="action-btn delete" onclick="deleteEntryFromCalendar(${evt.data.id})" title="Excluir"><i class="fa-solid fa-trash-can"></i></button>
                </div>
            `;
        } else {
            const clientName = evt.data.clientes ? evt.data.clientes.nome : 'Sem Cliente';
            item.innerHTML = `
                <div class="day-event-info">
                    <span class="day-event-title"><i class="fa-solid fa-clipboard-list" style="color: #a855f7; margin-right: 6px;"></i> Ordem de Serviço #${evt.data.id}</span>
                    <span class="day-event-sub">Cliente: ${clientName} | Status: ${evt.data.status}</span>
                </div>
                <span class="day-event-amount neutral">${formatCurrency(evt.data.total)}</span>
            `;
        }
        container.appendChild(item);
    });
    
    // Adicionar despesas (saídas)
    dayExpenses.forEach(exp => {
        const item = document.createElement('div');
        item.className = 'day-event-item';
        item.innerHTML = `
            <div class="day-event-info">
                <span class="day-event-title"><i class="fa-solid fa-arrow-down-left-from-square" style="color: #ef4444; margin-right: 6px;"></i> ${exp.descricao}</span>
                <span class="day-event-sub">Pagamento: ${exp.forma_pagamento}</span>
            </div>
            <div style="display: flex; align-items: center; gap: 12px;">
                <span class="day-event-amount negative">-${formatCurrency(exp.valor)}</span>
                <button class="action-btn delete" onclick="deleteExpenseFromCalendar(${exp.id})" title="Excluir"><i class="fa-solid fa-trash-can"></i></button>
            </div>
        `;
        container.appendChild(item);
    });
    
    if (dayEvents.length === 0 && dayExpenses.length === 0) {
        container.innerHTML = `<div style="text-align: center; color: var(--text-muted); padding: 20px; font-size:13.5px;"><i class="fa-solid fa-circle-info" style="margin-right: 6px;"></i>Nenhuma atividade registrada neste dia.</div>`;
    }
    
    openModal('modal-day-details');
}

async function deleteEntryFromCalendar(id) {
    if (!confirm('Deseja realmente excluir este serviço?')) return;
    try {
        const { error } = await supabaseClient.from('servicos_realizados').delete().eq('id', id);
        if (error) throw error;
        closeModal('modal-day-details');
        fetchCalendar();
    } catch (e) {
        alert('Erro ao excluir serviço: ' + e.message);
    }
}

async function deleteExpenseFromCalendar(id) {
    if (!confirm('Deseja realmente excluir esta despesa?')) return;
    try {
        const { error } = await supabaseClient.from('despesas').delete().eq('id', id);
        if (error) throw error;
        closeModal('modal-day-details');
        fetchCalendar();
    } catch (e) {
        alert('Erro ao excluir despesa: ' + e.message);
    }
}


// 💰 LÓGICA DO FLUXO DE CAIXA
function normalizePaymentMethod(method) {
    if (!method) return 'Outros';
    const m = method.trim().toUpperCase();
    if (m.includes('PIX')) return 'PIX';
    if (m.includes('DINHEIRO')) return 'Dinheiro';
    if (m.includes('BOLETO')) return 'Boleto';
    if (m.includes('CARTAO') || m.includes('CARTÃO') || m.includes('DÉBITO') || m.includes('DEBITO') || m.includes('CRÉDITO') || m.includes('CREDITO')) return 'Cartão';
    if (m.includes('CHEQUE')) return 'Cheque';
    return 'Outros';
}

async function fetchCaixa() {
    if (!supabaseClient) return;
    try {
        const year = currentCaixaDate.getFullYear();
        const month = currentCaixaDate.getMonth();
        const numDays = new Date(year, month + 1, 0).getDate();
        
        const startDate = new Date(year, month, 1, 0, 0, 0).toISOString();
        const endDate = new Date(year, month, numDays, 23, 59, 59).toISOString();
        
        // Atualizar título do mês
        const monthYearTitle = currentCaixaDate.toLocaleDateString('pt-BR', { month: 'long', year: 'numeric' });
        const capitalizedTitle = monthYearTitle.charAt(0).toUpperCase() + monthYearTitle.slice(1);
        const titleElem = document.getElementById('caixa-month-year-title');
        if (titleElem) {
            titleElem.textContent = capitalizedTitle;
        }
        
        // Buscar entradas, despesas e clientes
        const [entriesRes, expensesRes, clientsRes] = await Promise.all([
            supabaseClient.from('servicos_realizados').select('*, clientes(nome)').gte('data_servico', startDate).lte('data_servico', endDate),
            supabaseClient.from('despesas').select('*').gte('data_despesa', startDate).lte('data_despesa', endDate),
            supabaseClient.from('clientes').select('id, nome')
        ]);
        
        allMonthEntries = entriesRes.data || [];
        allMonthExpenses = expensesRes.data || [];
        
        // Preencher dropdown de clientes na modal de entrada
        const clientSelect = document.getElementById('entry-client-select');
        if (clientSelect && clientsRes.data) {
            clientSelect.innerHTML = '<option value="">Selecione um cliente...</option>';
            clientsRes.data.forEach(c => {
                clientSelect.innerHTML += `<option value="${c.id}">${c.nome}</option>`;
            });
        }
        
        // KPIs
        let totalEntradas = 0;
        allMonthEntries.forEach(e => totalEntradas += e.valor);
        
        let totalSaidas = 0;
        allMonthExpenses.forEach(exp => totalSaidas += exp.valor);
        
        const balance = totalEntradas - totalSaidas;
        
        document.getElementById('caixa-total-entradas').textContent = formatCurrency(totalEntradas);
        document.getElementById('caixa-total-saidas').textContent = formatCurrency(totalSaidas);
        
        const balanceValElem = document.getElementById('caixa-balance-value');
        if (balanceValElem) {
            balanceValElem.textContent = formatCurrency(balance);
        }
        
        const balanceCard = document.getElementById('caixa-balance-card');
        const balanceLabel = document.getElementById('caixa-balance-label');
        const balanceIcon = document.getElementById('caixa-balance-icon');
        
        if (balanceCard) {
            balanceCard.className = 'kpi-card';
            if (balance >= 0) {
                balanceCard.classList.add('profit-status');
                if (balanceLabel) balanceLabel.textContent = 'Lucro no Mês';
                if (balanceIcon) {
                    balanceIcon.className = 'kpi-icon green';
                    balanceIcon.innerHTML = '<i class="fa-solid fa-face-smile"></i>';
                }
            } else {
                balanceCard.classList.add('loss-status');
                if (balanceLabel) balanceLabel.textContent = 'Prejuízo no Mês';
                if (balanceIcon) {
                    balanceIcon.className = 'kpi-icon red';
                    balanceIcon.innerHTML = '<i class="fa-solid fa-face-frown"></i>';
                }
            }
        }
        
        // Resumo por Forma de Pagamento
        const methodSums = {
            'PIX': { inflow: 0, outflow: 0 },
            'Dinheiro': { inflow: 0, outflow: 0 },
            'Boleto': { inflow: 0, outflow: 0 },
            'Cartão': { inflow: 0, outflow: 0 },
            'Cheque': { inflow: 0, outflow: 0 },
            'Outros': { inflow: 0, outflow: 0 }
        };

        allMonthEntries.forEach(e => {
            const norm = normalizePaymentMethod(e.forma_pagamento);
            if (methodSums[norm]) {
                methodSums[norm].inflow += e.valor;
            } else {
                methodSums[norm] = { inflow: e.valor, outflow: 0 };
            }
        });

        allMonthExpenses.forEach(exp => {
            const norm = normalizePaymentMethod(exp.forma_pagamento);
            if (methodSums[norm]) {
                methodSums[norm].outflow += exp.valor;
            } else {
                methodSums[norm] = { inflow: 0, outflow: exp.valor };
            }
        });

        const summaryContainer = document.getElementById('caixa-payments-summary');
        if (summaryContainer) {
            summaryContainer.innerHTML = '';
            const methodsToRender = ['PIX', 'Dinheiro', 'Boleto', 'Cartão', 'Cheque'];
            if (methodSums['Outros'].inflow > 0 || methodSums['Outros'].outflow > 0) {
                methodsToRender.push('Outros');
            }
            
            methodsToRender.forEach(m => {
                const info = methodSums[m] || { inflow: 0, outflow: 0 };
                const bal = info.inflow - info.outflow;
                const balanceClass = bal >= 0 ? 'positive' : 'negative';
                const balanceSign = bal >= 0 ? '+' : '';
                
                const card = document.createElement('div');
                card.className = 'payment-summary-card';
                card.innerHTML = `
                    <div class="payment-method-name">${m}</div>
                    <div class="payment-method-details">
                        <div class="detail-row">
                            <span>Entradas:</span>
                            <span class="val-in">+${formatCurrency(info.inflow)}</span>
                        </div>
                        <div class="detail-row">
                            <span>Saídas:</span>
                            <span class="val-out">-${formatCurrency(info.outflow)}</span>
                        </div>
                        <div class="detail-row balance">
                            <span>Saldo:</span>
                            <span class="val-bal ${balanceClass}">${balanceSign}${formatCurrency(bal)}</span>
                        </div>
                    </div>
                `;
                summaryContainer.appendChild(card);
            });
        }
        
        renderCaixaTables();
    } catch (e) {
        console.error('Erro ao buscar caixa:', e);
    }
}

function renderCaixaTables() {
    const searchValElem = document.getElementById('caixa-search');
    const query = searchValElem ? searchValElem.value.toLowerCase() : '';
    
    // 1. Tabela de Entradas
    const entriesList = document.getElementById('caixa-entradas-list');
    if (entriesList) {
        entriesList.innerHTML = '';
        
        const filteredEntries = allMonthEntries.filter(e => {
            const cNome = e.clientes ? e.clientes.nome : '';
            const desc = e.descricao_servico || '';
            const forma = e.forma_pagamento || '';
            return cNome.toLowerCase().includes(query) || desc.toLowerCase().includes(query) || forma.toLowerCase().includes(query);
        });
        
        if (filteredEntries.length === 0) {
            entriesList.innerHTML = '<tr><td colspan="5" style="text-align: center; color: var(--color-text-muted);">Nenhuma entrada encontrada.</td></tr>';
        } else {
            filteredEntries.forEach(e => {
                const tr = document.createElement('tr');
                const dateFmt = new Date(e.data_servico).toLocaleDateString('pt-BR');
                const cNome = e.clientes ? e.clientes.nome : 'Sem Cliente';
                const labelPay = normalizePaymentMethod(e.forma_pagamento);
                
                tr.innerHTML = `
                    <td data-label="Data">${dateFmt}</td>
                    <td data-label="Cliente / Serviço">
                        <div style="font-weight: 500; color: var(--color-text-light);">${cNome}</div>
                        <div style="font-size: 0.85rem; color: var(--color-text-muted);">${e.descricao_servico}</div>
                    </td>
                    <td data-label="Forma">${labelPay}</td>
                    <td data-label="Valor" style="color: var(--color-green); font-weight: 600;">+${formatCurrency(e.valor)}</td>
                    <td data-label="Ações" class="actions-cell">
                        <button class="action-btn edit" onclick="editEntry(${e.id})" title="Editar"><i class="fa-solid fa-pen-to-square"></i></button>
                        <button class="action-btn delete" onclick="deleteEntry(${e.id})" title="Excluir"><i class="fa-solid fa-trash-can"></i></button>
                    </td>
                `;
                entriesList.appendChild(tr);
            });
        }
    }
    
    // 2. Tabela de Saídas
    const expensesList = document.getElementById('caixa-saidas-list');
    if (expensesList) {
        expensesList.innerHTML = '';
        
        const filteredExpenses = allMonthExpenses.filter(exp => {
            const desc = exp.descricao || '';
            const forma = exp.forma_pagamento || '';
            return desc.toLowerCase().includes(query) || forma.toLowerCase().includes(query);
        });
        
        if (filteredExpenses.length === 0) {
            expensesList.innerHTML = '<tr><td colspan="5" style="text-align: center; color: var(--color-text-muted);">Nenhuma saída encontrada.</td></tr>';
        } else {
            filteredExpenses.forEach(exp => {
                const tr = document.createElement('tr');
                const dateFmt = new Date(exp.data_despesa).toLocaleDateString('pt-BR');
                const labelPay = normalizePaymentMethod(exp.forma_pagamento);
                
                tr.innerHTML = `
                    <td data-label="Data">${dateFmt}</td>
                    <td data-label="Fornecedor / Descrição">${exp.descricao}</td>
                    <td data-label="Forma">${labelPay}</td>
                    <td data-label="Valor" style="color: var(--color-red); font-weight: 600;">-${formatCurrency(exp.valor)}</td>
                    <td data-label="Ações" class="actions-cell">
                        <button class="action-btn edit" onclick="editExpense(${exp.id})" title="Editar"><i class="fa-solid fa-pen-to-square"></i></button>
                        <button class="action-btn delete" onclick="deleteExpense(${exp.id})" title="Excluir"><i class="fa-solid fa-trash-can"></i></button>
                    </td>
                `;
                expensesList.appendChild(tr);
            });
        }
    }
}

// 📂 INSERÇÃO E EXCLUSÃO DO CAIXA
async function saveEntry(e) {
    e.preventDefault();
    const id = document.getElementById('entry-id-field').value;
    const msg = id ? 'Deseja realmente salvar as alterações desta entrada?' : 'Deseja realmente registrar esta entrada?';
    if (!confirm(msg)) return;

    const payment = document.getElementById('entry-payment').value;
    const installmentsInput = document.getElementById('entry-installments');
    const numInstallments = (payment === 'BOLETO' && installmentsInput) ? (parseInt(installmentsInput.value) || 1) : 1;
    const valorTotal = parseFloat(document.getElementById('entry-value').value);
    const clienteId = parseInt(document.getElementById('entry-client-select').value);
    const descricaoBase = document.getElementById('entry-desc').value.trim();

    try {
        let res;
        if (id) {
            // Editando entrada existente: edita como um lançamento individual normal
            const entry = {
                data_servico: new Date(document.getElementById('entry-date').value + 'T12:00:00').toISOString(),
                cliente_id: clienteId,
                descricao_servico: descricaoBase,
                valor: valorTotal,
                forma_pagamento: payment,
                num_parcelas: 1,
                valor_parcela: valorTotal
            };
            res = await supabaseClient.from('servicos_realizados').update(entry).eq('id', id);
        } else {
            // Criando entrada nova: se for boleto em várias parcelas, fazer múltiplos lançamentos
            if (payment === 'BOLETO' && numInstallments > 1) {
                const valorParcela = parseFloat((valorTotal / numInstallments).toFixed(2));
                const entriesToInsert = [];
                
                for (let i = 1; i <= numInstallments; i++) {
                    const dateInput = document.querySelector(`.entry-installment-date[data-index="${i}"]`);
                    const dateVal = dateInput ? dateInput.value : document.getElementById('entry-date').value;
                    const dataServico = new Date(dateVal + 'T12:00:00').toISOString();
                    
                    entriesToInsert.push({
                        data_servico: dataServico,
                        cliente_id: clienteId,
                        descricao_servico: `[Parc ${i}/${numInstallments}] ${descricaoBase}`,
                        valor: valorParcela,
                        forma_pagamento: 'BOLETO',
                        num_parcelas: numInstallments,
                        valor_parcela: valorParcela
                    });
                }
                res = await supabaseClient.from('servicos_realizados').insert(entriesToInsert);
            } else {
                const entry = {
                    data_servico: new Date(document.getElementById('entry-date').value + 'T12:00:00').toISOString(),
                    cliente_id: clienteId,
                    descricao_servico: descricaoBase,
                    valor: valorTotal,
                    forma_pagamento: payment,
                    num_parcelas: 1,
                    valor_parcela: valorTotal
                };
                res = await supabaseClient.from('servicos_realizados').insert([entry]);
            }
        }
        if (res.error) throw res.error;
        closeModal('modal-entry');
        fetchCaixa();
        if (currentView === 'dashboard') fetchCalendar();
    } catch (err) {
        alert('Erro ao salvar entrada: ' + err.message);
    }
}

async function deleteEntry(id) {
    if (!confirm('Deseja realmente excluir este lançamento de entrada?')) return;
    try {
        const { error } = await supabaseClient.from('servicos_realizados').delete().eq('id', id);
        if (error) throw error;
        fetchCaixa();
        if (currentView === 'dashboard') fetchCalendar();
    } catch (e) {
        alert('Erro ao excluir entrada: ' + e.message);
    }
}

async function editEntry(id) {
    if (!confirm('Deseja realmente editar esta entrada?')) return;
    try {
        const { data, error } = await supabaseClient.from('servicos_realizados').select('*').eq('id', id).single();
        if (error) throw error;
        
        document.getElementById('entry-id-field').value = data.id;
        document.getElementById('entry-date').value = data.data_servico ? data.data_servico.split('T')[0] : '';
        document.getElementById('entry-client-select').value = data.cliente_id;
        document.getElementById('entry-desc').value = data.descricao_servico;
        document.getElementById('entry-value').value = data.valor;
        document.getElementById('entry-payment').value = data.forma_pagamento;
        
        const installmentsGroup = document.getElementById('entry-installments-group');
        if (installmentsGroup) installmentsGroup.style.display = 'none';
        
        document.getElementById('entry-modal-title').textContent = 'Editar Lançamento de Entrada';
        document.getElementById('modal-entry').classList.remove('hidden');
    } catch (e) {
        console.error(e);
        alert('Erro ao carregar entrada para edição: ' + e.message);
    }
}

async function saveExpense(e) {
    e.preventDefault();
    const id = document.getElementById('expense-id-field').value;
    const msg = id ? 'Deseja realmente salvar as alterações desta saída?' : 'Deseja realmente registrar esta saída?';
    if (!confirm(msg)) return;

    const expense = {
        data_despesa: new Date(document.getElementById('expense-date').value + 'T12:00:00').toISOString(),
        descricao: document.getElementById('expense-desc').value.trim(),
        valor: parseFloat(document.getElementById('expense-value').value),
        forma_pagamento: document.getElementById('expense-payment').value
    };
    try {
        let res;
        if (id) {
            res = await supabaseClient.from('despesas').update(expense).eq('id', id);
        } else {
            res = await supabaseClient.from('despesas').insert([expense]);
        }
        if (res.error) throw res.error;
        closeModal('modal-expense');
        fetchCaixa();
        if (currentView === 'dashboard') fetchCalendar();
    } catch (err) {
        alert('Erro ao salvar despesa: ' + err.message);
    }
}

async function deleteExpense(id) {
    if (!confirm('Deseja realmente excluir este lançamento de despesa?')) return;
    try {
        const { error } = await supabaseClient.from('despesas').delete().eq('id', id);
        if (error) throw error;
        fetchCaixa();
        if (currentView === 'dashboard') fetchCalendar();
    } catch (e) {
        alert('Erro ao excluir despesa: ' + e.message);
    }
}

async function editExpense(id) {
    if (!confirm('Deseja realmente editar esta saída?')) return;
    try {
        const { data, error } = await supabaseClient.from('despesas').select('*').eq('id', id).single();
        if (error) throw error;
        
        document.getElementById('expense-id-field').value = data.id;
        document.getElementById('expense-date').value = data.data_despesa ? data.data_despesa.split('T')[0] : '';
        document.getElementById('expense-desc').value = data.descricao;
        document.getElementById('expense-value').value = data.valor;
        document.getElementById('expense-payment').value = data.forma_pagamento;
        
        document.getElementById('expense-modal-title').textContent = 'Editar Lançamento de Saída';
        document.getElementById('modal-expense').classList.remove('hidden');
    } catch (e) {
        console.error(e);
        alert('Erro ao carregar despesa para edição: ' + e.message);
    }
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
    const itemInput = document.getElementById('budget-item-input');
    if (itemInput) {
        itemInput.addEventListener('input', (e) => {
            const val = e.target.value;
            const type = itemType.value;
            const priceInput = document.getElementById('budget-item-price');
            
            if (type === 'PRODUTO') {
                const prod = allProducts.find(p => p.nome === val || `${p.nome} (Estoque: ${parseFloat(p.quantidade).toFixed(1)})` === val);
                if (prod) priceInput.value = prod.preco_venda;
            } else {
                const serv = allServices.find(s => s.nome === val || `${s.nome} (${s.tipo === 'POR_HORA' ? 'Hora' : 'Fixo'})` === val);
                if (serv) priceInput.value = serv.valor_base;
            }
        });
    }
}

async function openBudgetCreator(id) {
    const editId = (typeof id === 'number') ? id : null;
    if (editId && !confirm('Deseja realmente editar este orçamento?')) return;
    currentEditingBudgetId = editId;
    budgetCart = [];
    updateBudgetCartTable();
    
    const titleEl = document.querySelector('#budget-creator-panel .panel-title span');
    if (titleEl) {
        titleEl.textContent = editId ? `Editar Orçamento #${editId}` : 'Novo Orçamento';
    }
    
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

        if (editId) {
            // Buscar dados do orçamento
            const { data: budget, error: bErr } = await supabaseClient.from('orcamentos').select('*').eq('id', editId).single();
            if (bErr) throw bErr;
            
            // Setar cliente
            cliSelect.value = budget.cliente_id;
            
            // Buscar itens cadastrados
            const { data: items, error: iErr } = await supabaseClient.from('orcamento_itens').select('*').eq('orcamento_id', editId);
            if (iErr) throw iErr;
            
            // Carregar no carrinho
            budgetCart = items.map(item => ({
                id: item.id || Date.now() + Math.random(),
                tipo_item: item.tipo_item,
                produto_id: item.produto_id,
                servico_id: item.servico_id,
                descricao: item.descricao,
                quantidade: item.quantidade,
                valor_unitario: item.valor_unitario,
                valor_total: item.valor_total
            }));
            updateBudgetCartTable();
        }

        document.getElementById('budget-creator-panel').classList.remove('hidden');

    } catch (e) { alert('Erro ao carregar dados para o orçamento: ' + e.message); }
}

function closeBudgetCreator() {
    document.getElementById('budget-creator-panel').classList.add('hidden');
}

function populateBudgetCreatorItems() {
    const type = document.getElementById('budget-item-type').value;
    const datalist = document.getElementById('budget-item-datalist');
    const input = document.getElementById('budget-item-input');
    
    datalist.innerHTML = '';
    input.value = '';

    if (type === 'PRODUTO') {
        allProducts.forEach(p => {
            const option = document.createElement('option');
            option.value = `${p.nome} (Estoque: ${parseFloat(p.quantidade).toFixed(1)})`;
            datalist.appendChild(option);
        });
    } else {
        allServices.forEach(s => {
            const option = document.createElement('option');
            option.value = `${s.nome} (${s.tipo === 'POR_HORA' ? 'Hora' : 'Fixo'})`;
            datalist.appendChild(option);
        });
    }
    document.getElementById('budget-item-qty').value = '1';
    document.getElementById('budget-item-price').value = '';
}

function addBudgetCartItem() {
    const input = document.getElementById('budget-item-input');
    const val = input.value;
    const type = document.getElementById('budget-item-type').value;
    const qty = parseFloat(document.getElementById('budget-item-qty').value);
    const price = parseFloat(document.getElementById('budget-item-price').value);

    let item = null;
    if (type === 'PRODUTO') {
        item = allProducts.find(p => p.nome === val || `${p.nome} (Estoque: ${parseFloat(p.quantidade).toFixed(1)})` === val);
    } else {
        item = allServices.find(s => s.nome === val || `${s.nome} (${s.tipo === 'POR_HORA' ? 'Hora' : 'Fixo'})` === val);
    }

    if (!item || isNaN(qty) || qty <= 0 || isNaN(price) || price < 0) {
        alert('Por favor, selecione um item válido da lista e preencha a quantidade/preço.');
        return;
    }

    const itemId = item.id;
    const itemDesc = item.nome;

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
    input.value = '';
    document.getElementById('budget-item-qty').value = '1';
    document.getElementById('budget-item-price').value = '';
}

function removeBudgetCartItem(tempId) {
    if (!confirm('Deseja realmente remover este item do orçamento?')) return;
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

    const msg = currentEditingBudgetId ? 'Deseja realmente salvar as alterações deste orçamento?' : 'Deseja realmente criar este orçamento?';
    if (!confirm(msg)) return;

    const clientId = parseInt(clientVal);
    const totalGeral = budgetCart.reduce((sum, item) => sum + item.valor_total, 0);

    try {
        if (currentEditingBudgetId) {
            // 1. Atualizar orçamento existente
            const { error: bErr } = await supabaseClient.from('orcamentos').update({
                cliente_id: clientId,
                total: totalGeral
            }).eq('id', currentEditingBudgetId);
            if (bErr) throw bErr;

            // 2. Apagar itens antigos
            const { error: delErr } = await supabaseClient.from('orcamento_itens').delete().eq('orcamento_id', currentEditingBudgetId);
            if (delErr) throw delErr;

            // 3. Formatar novos itens
            const itemsToInsert = budgetCart.map(item => ({
                orcamento_id: currentEditingBudgetId,
                tipo_item: item.tipo_item,
                produto_id: item.produto_id,
                servico_id: item.servico_id,
                descricao: item.descricao,
                quantidade: item.quantidade,
                valor_unitario: item.valor_unitario,
                valor_total: item.valor_total
            }));

            // 4. Inserir novos itens
            const { error: insErr } = await supabaseClient.from('orcamento_itens').insert(itemsToInsert);
            if (insErr) throw insErr;

            alert(`Orçamento #${currentEditingBudgetId} atualizado com sucesso!`);
        } else {
            // Criando novo orçamento
            const budget = {
                cliente_id: clientId,
                total: totalGeral,
                status: 'ABERTO'
            };

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
        }

        closeBudgetCreator();
        fetchBudgets();
        switchView('budgets');

    } catch (e) {
        alert('Erro ao salvar orçamento: ' + e.message);
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

// Agenda simples de lembretes locais
const AGENDA_STORAGE_KEY = 'tec_agenda_reminders';

function getAgendaReminders() {
    try {
        return JSON.parse(localStorage.getItem(AGENDA_STORAGE_KEY)) || {};
    } catch (e) {
        return {};
    }
}

function saveAgendaReminders(reminders) {
    localStorage.setItem(AGENDA_STORAGE_KEY, JSON.stringify(reminders));
}

function setupAgendaReminderEvents() {
    const form = document.getElementById('agenda-reminder-form');
    const deleteBtn = document.getElementById('btn-delete-agenda-reminder');

    if (form) {
        form.addEventListener('submit', (e) => {
            e.preventDefault();
            const date = document.getElementById('agenda-reminder-date').value;
            const text = document.getElementById('agenda-reminder-text').value.trim();
            const reminders = getAgendaReminders();

            if (text) {
                reminders[date] = text;
            } else {
                delete reminders[date];
            }

            saveAgendaReminders(reminders);
            closeModal('modal-agenda-reminder');
            fetchCalendar();
        });
    }

    if (deleteBtn) {
        deleteBtn.addEventListener('click', () => {
            const date = document.getElementById('agenda-reminder-date').value;
            const reminders = getAgendaReminders();
            delete reminders[date];
            saveAgendaReminders(reminders);
            closeModal('modal-agenda-reminder');
            fetchCalendar();
        });
    }
}

function fetchCalendar() {
    renderCalendar();
}

function renderCalendar() {
    const grid = document.getElementById('calendar-days-grid');
    const title = document.getElementById('calendar-month-year-title');
    if (!grid || !title) return;

    const reminders = getAgendaReminders();
    const year = currentCalendarDate.getFullYear();
    const month = currentCalendarDate.getMonth();
    const monthYearTitle = currentCalendarDate.toLocaleDateString('pt-BR', { month: 'long', year: 'numeric' });
    title.textContent = monthYearTitle;
    grid.innerHTML = '';

    ['Dom', 'Seg', 'Ter', 'Qua', 'Qui', 'Sex', 'Sab'].forEach(day => {
        const div = document.createElement('div');
        div.className = 'calendar-day-header';
        div.textContent = day;
        grid.appendChild(div);
    });

    const firstDayIndex = new Date(year, month, 1).getDay();
    const numDays = new Date(year, month + 1, 0).getDate();
    const prevLastDay = new Date(year, month, 0).getDate();
    const today = new Date();
    const isCurrentMonth = today.getFullYear() === year && today.getMonth() === month;

    for (let x = firstDayIndex; x > 0; x--) {
        const dayNum = prevLastDay - x + 1;
        const cell = document.createElement('div');
        cell.className = 'calendar-day other-month';
        cell.innerHTML = `<span class="calendar-day-number">${dayNum}</span>`;
        grid.appendChild(cell);
    }

    for (let day = 1; day <= numDays; day++) {
        const dateString = `${year}-${String(month + 1).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
        const reminder = reminders[dateString] || '';
        const cell = document.createElement('button');
        cell.type = 'button';
        cell.className = 'calendar-day agenda-day';
        if (isCurrentMonth && today.getDate() === day) cell.classList.add('today');
        if (reminder) cell.classList.add('has-reminder');

        cell.innerHTML = `
            <span class="calendar-day-number">${day}</span>
            <div class="calendar-events">
                ${reminder ? `<span class="calendar-badge badge-reminder" title="${escapeHtml(reminder)}">${escapeHtml(reminder)}</span>` : '<span class="agenda-empty">Adicionar lembrete</span>'}
            </div>
        `;

        cell.addEventListener('click', () => openAgendaReminderModal(dateString));
        grid.appendChild(cell);
    }

    const totalCells = firstDayIndex + numDays;
    const remaining = 42 - totalCells;
    for (let day = 1; day <= remaining; day++) {
        const cell = document.createElement('div');
        cell.className = 'calendar-day other-month';
        cell.innerHTML = `<span class="calendar-day-number">${day}</span>`;
        grid.appendChild(cell);
    }
}

function openAgendaReminderModal(dateString) {
    const reminders = getAgendaReminders();
    const date = new Date(`${dateString}T00:00:00`);
    const formattedDate = date.toLocaleDateString('pt-BR', {
        weekday: 'long',
        day: '2-digit',
        month: '2-digit',
        year: 'numeric'
    });
    const reminderText = reminders[dateString] || '';

    document.getElementById('agenda-reminder-title').textContent = `Lembrete - ${formattedDate}`;
    document.getElementById('agenda-reminder-date').value = dateString;
    document.getElementById('agenda-reminder-text').value = reminderText;
    document.getElementById('btn-delete-agenda-reminder').style.display = reminderText ? 'inline-flex' : 'none';
    openModal('modal-agenda-reminder');
}

function escapeHtml(value) {
    return String(value)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;');
}

// Registro do Service Worker para PWA
if ('serviceWorker' in navigator) {
    window.addEventListener('load', () => {
        navigator.serviceWorker.register('sw.js')
            .then(reg => console.log('Service Worker registrado com sucesso:', reg.scope))
            .catch(err => console.error('Erro ao registrar o Service Worker:', err));
    });
}
