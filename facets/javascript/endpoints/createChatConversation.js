const createChatConversation = async (parameters) =>  {
	const baseUrl = window.location.origin;
	const url = new URL(`${window.location.pathname.split('/')[1]}/rest/createChatConversation/`, baseUrl);
	return fetch(url.toString(), {
		method: 'POST', 
		headers : new Headers({
 			'Content-Type': 'application/json'
		}),
		body: JSON.stringify({
			createrWalletId : parameters.createrWalletId,
			title : parameters.title
		})
	});
}

const createChatConversationForm = (container) => {
	const html = `<form id='createChatConversation-form'>
		<div id='createChatConversation-createrWalletId-form-field'>
			<label for='createrWalletId'>createrWalletId</label>
			<input type='text' id='createChatConversation-createrWalletId-param' name='createrWalletId'/>
		</div>
		<div id='createChatConversation-title-form-field'>
			<label for='title'>title</label>
			<input type='text' id='createChatConversation-title-param' name='title'/>
		</div>
		<button type='button'>Test</button>
	</form>`;

	container.insertAdjacentHTML('beforeend', html)

	const createrWalletId = container.querySelector('#createChatConversation-createrWalletId-param');
	const title = container.querySelector('#createChatConversation-title-param');

	container.querySelector('#createChatConversation-form button').onclick = () => {
		const params = {
			createrWalletId : createrWalletId.value !== "" ? createrWalletId.value : undefined,
			title : title.value !== "" ? title.value : undefined
		};

		createChatConversation(params).then(r => r.text().then(
				t => alert(t)
			));
	};
}

export { createChatConversation, createChatConversationForm };