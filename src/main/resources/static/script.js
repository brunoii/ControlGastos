document.addEventListener('DOMContentLoaded', () => {
  cargarMeses();
  actualizarDatos();
  cargarCategorias();

  document.getElementById('formIngreso')?.addEventListener('submit', guardarIngreso);
  document.getElementById('formGasto')?.addEventListener('submit', guardarGasto);
  document.getElementById('esTerceroCheck')?.addEventListener('change', toggleTercero);
  document.getElementById('cuotasCheck')?.addEventListener('change', toggleCuotas);
  document.getElementById('usuarioSelect')?.addEventListener('change', actualizarDatos);
  document.getElementById('mesSelect')?.addEventListener('change', actualizarDatos);
  
  document.getElementById('btnNuevoIngreso').addEventListener('click', abrirModalIngreso);
  document.getElementById('btnNuevoGasto').addEventListener('click', abrirModal);
  document.getElementById('btnCerrarMes').addEventListener('click', cerrarMes);

  // Delegación para editar/eliminar gastos
  document.getElementById('tablaGastos').addEventListener('click', (e) => {
	  if (e.target.classList.contains('btn-editar-gasto')) {
	    const id = e.target.dataset.id;
	    editarGasto(id);
	  } else if (e.target.classList.contains('btn-eliminar-gasto')) {
	    const id = e.target.dataset.id;
	    eliminarGasto(id);
	  }
	  });
	  

	  // Delegación para editar/eliminar ingresos
	  document.getElementById('tablaIngresos').addEventListener('click', (e) => {
	    if (e.target.classList.contains('btn-editar-ingreso')) {
	      const id = e.target.dataset.id;
	      editarIngreso(id);
	    } else if (e.target.classList.contains('btn-eliminar-ingreso')) {
	      const id = e.target.dataset.id;
	      eliminarIngreso(id);
	    }
	  });

	  // Delegación para editar cuotas (solo editar)
	  document.getElementById('tablaCuotas').addEventListener('click', (e) => {
	    if (e.target.classList.contains('btn-editar-gasto')) {
	      const id = e.target.dataset.id;
	      editarGasto(id);
	    }
	  });
	  
});

function formatearNumero(num) {
  return num.toLocaleString('es-AR', { style: 'currency', currency: 'ARS' });
}

function toggleTabla(id) {
  const wrapper = document.getElementById(id);
  wrapper.style.display = wrapper.style.display === 'none' ? 'block' : 'none';
}

async function cargarMeses() {
  const res = await fetch('/api/meses');
  const meses = await res.json();
  const select = document.getElementById('mesSelect');
  select.innerHTML = '';
  meses.forEach(mes => {
    const option = document.createElement('option');
    option.value = mes.id;
    option.textContent = mes.nombre;
    select.appendChild(option);
  });
}

async function cargarCategorias() {
  const res = await fetch('/api/categorias');
  const categorias = await res.json();
  const select = document.getElementById('categoriaSelect');
  categorias.forEach(c => {
    const opt = document.createElement('option');
    opt.value = c.id;
    opt.textContent = c.nombre;
    select.appendChild(opt);
  });
}

async function actualizarDatos() {
  const usuarioId = document.getElementById('usuarioSelect').value;
  const mesId = document.getElementById('mesSelect').value;
  if (!mesId) return;

  const [gastosRes, cuotasRes, ingresosRes] = await Promise.all([
    fetch(`/api/gastos/usuario/${usuarioId}/mes/${mesId}`),
    fetch(`/api/cuotas/mes/${mesId}/usuario/${usuarioId}`),
    fetch(`/api/ingresos/usuario/${usuarioId}/mes/${mesId}`)
  ]);

  const gastos = await gastosRes.json();
  const cuotas = await cuotasRes.json();
  const ingresos = await ingresosRes.json();

  const gastosAlContado = gastos.filter(g => g.medioPago !== 'CREDITO');

  renderGastos(gastosAlContado);
  renderCuotas(cuotas);
  renderIngresos(ingresos);
  renderResumen(gastosAlContado, cuotas, ingresos);
}

function renderGastos(gastos) {
  const tbody = document.getElementById('tablaGastos');
  const tbodyTerceros = document.getElementById('tablaTerceros');
  tbody.innerHTML = '';
  tbodyTerceros.innerHTML = '';

  gastos.forEach(g => {
    const fila = document.createElement('tr');
    fila.innerHTML = `
      <td>${g.fechaOperacion}</td>
      <td>${g.descripcion}</td>
      <td>${formatearNumero(g.montoTotal)}</td>
      <td>${g.medioPago}</td>
      <td>${g.categoria?.nombre || '-'}</td>
      <td>
		  <button class="btn btn-sm btn-outline-primary btn-editar-gasto" data-id="${g.id}">✏️</button>
		  <button class="btn btn-sm btn-outline-danger btn-eliminar-gasto" data-id="${g.id}">🗑️</button>
      </td>
    `;
    if (g.esTercero) tbodyTerceros.appendChild(fila);
    else tbody.appendChild(fila);
  });
}

function renderResumen(gastos, cuotas, ingresos) {
  const totalGastos = gastos.filter(g => !g.esTercero).reduce((sum, g) => sum + g.montoTotal, 0);
  const totalCuotas = cuotas.reduce((sum, c) => sum + c.montoCuota, 0);
  const totalIngresos = ingresos.reduce((sum, i) => sum + i.monto, 0);
  const saldo = totalIngresos - totalGastos - totalCuotas;

  document.getElementById('ingresosTotal').textContent = formatearNumero(totalIngresos);
  document.getElementById('gastosTotal').textContent = formatearNumero(totalGastos);
  document.getElementById('cuotasTotal').textContent = formatearNumero(totalCuotas);

  const saldoEl = document.getElementById('saldo');
  saldoEl.textContent = formatearNumero(saldo);
  saldoEl.className = saldo < 0 ? 'negativo' : 'positivo';
}

function renderCuotas(cuotas) {
  const tbody = document.getElementById('tablaCuotas');
  tbody.innerHTML = '';
  cuotas.forEach(c => {
    const fila = document.createElement('tr');
    fila.innerHTML = `
      <td>${c.gasto.fechaOperacion}</td>
      <td>${c.gasto.descripcion}</td>
      <td>${formatearNumero(c.montoCuota)}</td>
      <td>${c.numeroCuota}/${c.totalCuotas}</td>
      <td><button class="btn btn-sm btn-outline-primary btn-editar-gasto" data-id="${c.gasto.id}">✏️</button></td>
    `;
    tbody.appendChild(fila);
  });
}

function renderIngresos(ingresos) {
  const tbody = document.getElementById('tablaIngresos');
  tbody.innerHTML = '';
  ingresos.forEach(i => {
    const fila = document.createElement('tr');
    fila.innerHTML = `
      <td>${i.fecha}</td>
      <td>${i.descripcion}</td>
      <td>${formatearNumero(i.monto)}</td>
	  <td>
	     <button class="btn btn-sm btn-outline-primary btn-editar-ingreso" data-id="${i.id}">✏️</button>
	     <button class="btn btn-sm btn-outline-danger btn-eliminar-ingreso" data-id="${i.id}">🗑️</button>
	  </td>
    `;
    tbody.appendChild(fila);
  });
}

// Modal Gasto
function abrirModal() {
	console.log('Abrir modal gasto');
  document.getElementById('modal').classList.remove('hidden');
}
function cerrarModal() {
  document.getElementById('modal').classList.add('hidden');
}

// Modal Ingreso
function abrirModalIngreso() {
	console.log('Abrir modal ingreso');
  document.getElementById('modalIngreso').classList.remove('hidden');
}
function cerrarModalIngreso() {
  document.getElementById('modalIngreso').classList.add('hidden');
}

function toggleTercero(e) {
  document.getElementById('datosTercero').style.display = e.target.checked ? 'block' : 'none';
}
function toggleCuotas(e) {
  document.getElementById('datosCuotas').style.display = e.target.checked ? 'block' : 'none';
}

async function guardarGasto(e) {
  e.preventDefault();
  const form = e.target;
  const usuarioId = document.getElementById('usuarioSelect').value;

  const data = {
    montoTotal: parseFloat(form.montoTotal.value),
    fechaOperacion: form.fechaOperacion.value,
    medioPago: form.medioPago.value,
    descripcion: form.descripcion.value,
    categoria: { id: parseInt(form.categoriaId.value) },
    usuario: { id: parseInt(usuarioId) },
    esTercero: form.esTercero.checked,
    nombreTercero: form.nombreTercero?.value || null,
    fueReembolsado: form.fueReembolsado?.checked || false,
    cuotas: form.tieneCuotas.checked ? parseInt(form.cuotas.value) : 0,
    mesInicioCuotas: form.tieneCuotas.checked ? form.mesInicioCuotas.value : null
  };

  const url = form.dataset.id ? `/api/gastos/${form.dataset.id}` : '/api/gastos';
  const metodo = form.dataset.id ? 'PUT' : 'POST';

  const res = await fetch(url, {
    method: metodo,
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data)
  });

  if (res.ok) {
    cerrarModal();
    actualizarDatos();
  } else {
    alert('Error al guardar gasto.');
  }

  delete form.dataset.id;
}

async function editarGasto(id) {
  const res = await fetch(`/api/gastos/${id}`);
  const gasto = await res.json();

  const form = document.getElementById('formGasto');
  form.montoTotal.value = gasto.montoTotal;
  form.fechaOperacion.value = gasto.fechaOperacion;
  form.medioPago.value = gasto.medioPago;
  form.descripcion.value = gasto.descripcion;
  form.categoriaId.value = gasto.categoria.id;
  form.esTercero.checked = gasto.esTercero;
  toggleTercero({ target: form.esTercero });
  form.nombreTercero.value = gasto.nombreTercero || '';
  form.fueReembolsado.checked = gasto.fueReembolsado || false;
  form.tieneCuotas.checked = gasto.cuotas > 0;
  toggleCuotas({ target: form.tieneCuotas });
  form.cuotas.value = gasto.cuotas || '';
  form.mesInicioCuotas.value = gasto.mesInicioCuotas || '';
  form.dataset.id = gasto.id;
  abrirModal();
}

async function eliminarGasto(id) {
  if (!confirm('¿Eliminar este gasto?')) return;
  const res = await fetch(`/api/gastos/${id}`, { method: 'DELETE' });
  if (res.ok) actualizarDatos();
  else alert('Error al eliminar gasto');
}

async function guardarIngreso(e) {
  e.preventDefault();
  const form = e.target;
  const usuarioId = parseInt(document.getElementById('usuarioSelect').value);
  const mesId = parseInt(document.getElementById('mesSelect').value);

  const data = {
    monto: parseFloat(form.monto.value),
    fecha: form.fecha.value,
    descripcion: form.descripcion.value,
    usuario: { id: usuarioId },
    mesContable: { id: mesId }
  };

  const metodo = form.dataset.id ? 'PUT' : 'POST';
  const url = form.dataset.id ? `/api/ingresos/${form.dataset.id}` : '/api/ingresos';

  const res = await fetch(url, {
    method: metodo,
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data)
  });

  if (res.ok) {
    cerrarModalIngreso();
    actualizarDatos();
  } else {
    alert('Error al guardar ingreso');
  }

  delete form.dataset.id;
}

async function editarIngreso(id) {
  const res = await fetch(`/api/ingresos/${id}`);
  const ingreso = await res.json();

  const form = document.getElementById('formIngreso');
  form.monto.value = ingreso.monto;
  form.fecha.value = ingreso.fecha;
  form.descripcion.value = ingreso.descripcion || '';
  form.dataset.id = ingreso.id;

  abrirModalIngreso();
}

async function eliminarIngreso(id) {
  if (!confirm('¿Eliminar este ingreso?')) return;
  const res = await fetch(`/api/ingresos/${id}`, { method: 'DELETE' });
  if (res.ok) actualizarDatos();
  else alert('Error al eliminar ingreso');
}

async function cerrarMes() {
  const usuarioId = document.getElementById('usuarioSelect').value;
  const mesId = document.getElementById('mesSelect').value;
  const res = await fetch(`/api/meses/cerrar?mesId=${mesId}&usuarioId=${usuarioId}`, {
    method: 'POST'
  });
  if (res.ok) {
    alert('Mes cerrado correctamente');
    await cargarMeses();
    actualizarDatos();
  } else {
    alert('Error al cerrar mes');
  }
}

window.abrirModal = abrirModal;
window.cerrarModal = cerrarModal;
window.abrirModalIngreso = abrirModalIngreso;
window.cerrarModalIngreso = cerrarModalIngreso;

window.editarGasto = editarGasto;
window.eliminarGasto = eliminarGasto;
window.editarIngreso = editarIngreso;
window.eliminarIngreso = eliminarIngreso;

window.toggleTabla = toggleTabla;