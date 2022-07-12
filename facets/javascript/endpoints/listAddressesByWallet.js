const listAddressesByWallet = async (parameters) =>  {
	const baseUrl = window.location.origin;
	const url = new URL(`${window.location.pathname.split('/')[1]}/rest/listAddressesByWallet/${parameters.walletId}`, baseUrl);
	return fetch(url.toString(), {
		method: 'GET'
	});
}

const listAddressesByWalletForm = (container) => {
	const html = `<form id='listAddressesByWallet-form'>
		<div id='listAddressesByWallet-walletId-form-field'>
			<label for='walletId'>walletId</label>
			<input type='text' id='listAddressesByWallet-walletId-param' name='walletId'/>
		</div>
		<button type='button'>Test</button>
	</form>`;

	container.insertAdjacentHTML('beforeend', html)

	const walletId = container.querySelector('#listAddressesByWallet-walletId-param');

	container.querySelector('#listAddressesByWallet-form button').onclick = () => {
		const params = {
			walletId : walletId.value !== "" ? walletId.value : undefined
		};

		listAddressesByWallet(params).then(r => r.text().then(
				t => alert(t)
			));
	};
}

export { listAddressesByWallet, listAddressesByWalletForm };