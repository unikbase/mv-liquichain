const getChatConversation = async (parameters) =>  {
	const baseUrl = window.location.origin;
	const url = new URL(`${window.location.pathname.split('/')[1]}/rest/getChatConversation/${parameters.uuId}`, baseUrl);
	return fetch(url.toString(), {
		method: 'GET'
	});
}

const getChatConversationForm = (container) => {
	const html = `<form id='getChatConversation-form'>
		<div id='getChatConversation-uuId-form-field'>
			<label for='uuId'>uuId</label>
			<input type='text' id='getChatConversation-uuId-param' name='uuId'/>
		</div>
		<button type='button'>Test</button>
	</form>`;

	container.insertAdjacentHTML('beforeend', html)

	const uuId = container.querySelector('#getChatConversation-uuId-param');

	container.querySelector('#getChatConversation-form button').onclick = () => {
		const params = {
			uuId : uuId.value !== "" ? uuId.value : undefined
		};

		getChatConversation(params).then(r => r.text().then(
				t => alert(t)
			));
	};
}

export { getChatConversation, getChatConversationForm };