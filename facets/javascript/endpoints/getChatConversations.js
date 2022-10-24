const getChatConversations = async (parameters) =>  {
	const baseUrl = window.location.origin;
	const url = new URL(`${window.location.pathname.split('/')[1]}/rest/getChatConversations/${parameters.walletId}`, baseUrl);
	return fetch(url.toString(), {
		method: 'GET'
	});
}

const getChatConversationsForm = (container) => {
	const html = `<form id='getChatConversations-form'>
		<div id='getChatConversations-walletId-form-field'>
			<label for='walletId'>walletId</label>
			<input type='text' id='getChatConversations-walletId-param' name='walletId'/>
		</div>
		<button type='button'>Test</button>
	</form>`;

	container.insertAdjacentHTML('beforeend', html)

	const walletId = container.querySelector('#getChatConversations-walletId-param');

	container.querySelector('#getChatConversations-form button').onclick = () => {
		const params = {
			walletId : walletId.value !== "" ? walletId.value : undefined
		};

		getChatConversations(params).then(r => r.text().then(
				t => alert(t)
			));
	};
}

export { getChatConversations, getChatConversationsForm };