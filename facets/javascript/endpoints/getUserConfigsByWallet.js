const getUserConfigsByWallet = async (parameters) =>  {
	const baseUrl = window.location.origin;
	const url = new URL(`${window.location.pathname.split('/')[1]}/rest/getUserConfigsByWallet/`, baseUrl);
	if (parameters.walletId !== undefined) {
		url.searchParams.append('walletId', parameters.walletId);
	}

	return fetch(url.toString(), {
		method: 'GET'
	});
}

const getUserConfigsByWalletForm = (container) => {
	const html = `<form id='getUserConfigsByWallet-form'>
		<div id='getUserConfigsByWallet-walletId-form-field'>
			<label for='walletId'>walletId</label>
			<input type='text' id='getUserConfigsByWallet-walletId-param' name='walletId'/>
		</div>
		<button type='button'>Test</button>
	</form>`;

	container.insertAdjacentHTML('beforeend', html)

	const walletId = container.querySelector('#getUserConfigsByWallet-walletId-param');

	container.querySelector('#getUserConfigsByWallet-form button').onclick = () => {
		const params = {
			walletId : walletId.value !== "" ? walletId.value : undefined
		};

		getUserConfigsByWallet(params).then(r => r.text().then(
				t => alert(t)
			));
	};
}

export { getUserConfigsByWallet, getUserConfigsByWalletForm };