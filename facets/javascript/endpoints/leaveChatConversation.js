const leaveChatConversation = async (parameters) =>  {
	const baseUrl = window.location.origin;
	const url = new URL(`${window.location.pathname.split('/')[1]}/rest/leaveChatConversation/`, baseUrl);
	return fetch(url.toString(), {
		method: 'POST', 
		headers : new Headers({
 			'Content-Type': 'application/json'
		}),
		body: JSON.stringify({
			chatConversationId : parameters.chatConversationId,
			participantWalletId : parameters.participantWalletId
		})
	});
}

const leaveChatConversationForm = (container) => {
	const html = `<form id='leaveChatConversation-form'>
		<div id='leaveChatConversation-chatConversationId-form-field'>
			<label for='chatConversationId'>chatConversationId</label>
			<input type='text' id='leaveChatConversation-chatConversationId-param' name='chatConversationId'/>
		</div>
		<div id='leaveChatConversation-participantWalletId-form-field'>
			<label for='participantWalletId'>participantWalletId</label>
			<input type='text' id='leaveChatConversation-participantWalletId-param' name='participantWalletId'/>
		</div>
		<button type='button'>Test</button>
	</form>`;

	container.insertAdjacentHTML('beforeend', html)

	const chatConversationId = container.querySelector('#leaveChatConversation-chatConversationId-param');
	const participantWalletId = container.querySelector('#leaveChatConversation-participantWalletId-param');

	container.querySelector('#leaveChatConversation-form button').onclick = () => {
		const params = {
			chatConversationId : chatConversationId.value !== "" ? chatConversationId.value : undefined,
			participantWalletId : participantWalletId.value !== "" ? participantWalletId.value : undefined
		};

		leaveChatConversation(params).then(r => r.text().then(
				t => alert(t)
			));
	};
}

export { leaveChatConversation, leaveChatConversationForm };