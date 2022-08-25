const getBlockedUsersList = async (parameters) =>  {
	const baseUrl = window.location.origin;
	const url = new URL(`${window.location.pathname.split('/')[1]}/rest/getBlockedUsersList/`, baseUrl);
	if (parameters.walletId !== undefined) {
		url.searchParams.append('walletId', parameters.walletId);
	}

	return fetch(url.toString(), {
		method: 'GET'
	});
}

const getBlockedUsersListForm = (container) => {
	const html = `<form id='getBlockedUsersList-form'>
		<div id='getBlockedUsersList-walletId-form-field'>
			<label for='walletId'>walletId</label>
			<input type='text' id='getBlockedUsersList-walletId-param' name='walletId'/>
		</div>
		<button type='button'>Test</button>
	</form>`;

	container.insertAdjacentHTML('beforeend', html)

	const walletId = container.querySelector('#getBlockedUsersList-walletId-param');

	container.querySelector('#getBlockedUsersList-form button').onclick = () => {
		const params = {
			walletId : walletId.value !== "" ? walletId.value : undefined
		};

		getBlockedUsersList(params).then(r => r.text().then(
				t => alert(t)
			));
	};
}

export { getBlockedUsersList, getBlockedUsersListForm };