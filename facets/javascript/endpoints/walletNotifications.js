const walletNotifications = async (parameters) =>  {
	const baseUrl = window.location.origin;
	const url = new URL(`${window.location.pathname.split('/')[1]}/rest/walletNotifications/${parameters.walletId}`, baseUrl);
	return fetch(url.toString(), {
		method: 'GET'
	});
}

const walletNotificationsForm = (container) => {
	const html = `<form id='walletNotifications-form'>
		<div id='walletNotifications-walletId-form-field'>
			<label for='walletId'>walletId</label>
			<input type='text' id='walletNotifications-walletId-param' name='walletId'/>
		</div>
		<button type='button'>Test</button>
	</form>`;

	container.insertAdjacentHTML('beforeend', html)

	const walletId = container.querySelector('#walletNotifications-walletId-param');

	container.querySelector('#walletNotifications-form button').onclick = () => {
		const params = {
			walletId : walletId.value !== "" ? walletId.value : undefined
		};

		walletNotifications(params).then(r => r.text().then(
				t => alert(t)
			));
	};
}

export { walletNotifications, walletNotificationsForm };