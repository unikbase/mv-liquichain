const isUserBlockedByUsers = async (parameters) =>  {
	const baseUrl = window.location.origin;
	const url = new URL(`${window.location.pathname.split('/')[1]}/rest/isUserBlockedByUsers/`, baseUrl);
	if (parameters.walletId !== undefined) {
		url.searchParams.append('walletId', parameters.walletId);
	}

	if (parameters.blockerWalletIds !== undefined) {
	for(let v of parameters.blockerWalletIds){		url.searchParams.append('blockerWalletIds', v);
	}
	}

	return fetch(url.toString(), {
		method: 'GET'
	});
}

const isUserBlockedByUsersForm = (container) => {
	const html = `<form id='isUserBlockedByUsers-form'>
		<div id='isUserBlockedByUsers-null-form-field'>
			<label for='null'>null</label>
			<input type='text' id='isUserBlockedByUsers-null-param' name='null'/>
		</div>
		<div id='isUserBlockedByUsers-null-form-field'>
			<label for='null'>null</label>
			<input type='text' id='isUserBlockedByUsers-null-param' name='null'/>
		</div>
		<button type='button'>Test</button>
	</form>`;

	container.insertAdjacentHTML('beforeend', html)

	const null = container.querySelector('#isUserBlockedByUsers-null-param');
	const null = container.querySelector('#isUserBlockedByUsers-null-param');

	container.querySelector('#isUserBlockedByUsers-form button').onclick = () => {
		const params = {
			null : null.value !== "" ? null.value : undefined,
			null : null.value !== "" ? null.value : undefined
		};

		isUserBlockedByUsers(params).then(r => r.text().then(
				t => alert(t)
			));
	};
}

export { isUserBlockedByUsers, isUserBlockedByUsersForm };