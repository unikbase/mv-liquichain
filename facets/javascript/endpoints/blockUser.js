const blockUser = async (parameters) =>  {
	const baseUrl = window.location.origin;
	const url = new URL(`${window.location.pathname.split('/')[1]}/rest/blockUser/`, baseUrl);
	return fetch(url.toString(), {
		method: 'POST', 
		headers : new Headers({
 			'Content-Type': 'application/json'
		}),
		body: JSON.stringify({
			walletId : parameters.walletId,
			targetWalletId : parameters.targetWalletId,
			blocked : parameters.blocked
		})
	});
}

const blockUserForm = (container) => {
	const html = `<form id='blockUser-form'>
		<div id='blockUser-walletId-form-field'>
			<label for='walletId'>walletId</label>
			<input type='text' id='blockUser-walletId-param' name='walletId'/>
		</div>
		<div id='blockUser-targetWalletId-form-field'>
			<label for='targetWalletId'>targetWalletId</label>
			<input type='text' id='blockUser-targetWalletId-param' name='targetWalletId'/>
		</div>
		<div id='blockUser-blocked-form-field'>
			<label for='blocked'>blocked</label>
			<input type='text' id='blockUser-blocked-param' name='blocked'/>
		</div>
		<button type='button'>Test</button>
	</form>`;

	container.insertAdjacentHTML('beforeend', html)

	const walletId = container.querySelector('#blockUser-walletId-param');
	const targetWalletId = container.querySelector('#blockUser-targetWalletId-param');
	const blocked = container.querySelector('#blockUser-blocked-param');

	container.querySelector('#blockUser-form button').onclick = () => {
		const params = {
			walletId : walletId.value !== "" ? walletId.value : undefined,
			targetWalletId : targetWalletId.value !== "" ? targetWalletId.value : undefined,
			blocked : blocked.value !== "" ? blocked.value : undefined
		};

		blockUser(params).then(r => r.text().then(
				t => alert(t)
			));
	};
}

export { blockUser, blockUserForm };