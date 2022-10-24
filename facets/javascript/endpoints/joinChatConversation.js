const joinChatConversation = async (parameters) =>  {
	const baseUrl = window.location.origin;
	const url = new URL(`${window.location.pathname.split('/')[1]}/rest/joinChatConversation/`, baseUrl);
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

const joinChatConversationForm = (container) => {
	const html = `<form id='joinChatConversation-form'>
		<div id='joinChatConversation-chatConversationId-form-field'>
			<label for='chatConversationId'>chatConversationId</label>
			<input type='text' id='joinChatConversation-chatConversationId-param' name='chatConversationId'/>
		</div>
		<div id='joinChatConversation-participantWalletId-form-field'>
			<label for='participantWalletId'>participantWalletId</label>
			<input type='text' id='joinChatConversation-participantWalletId-param' name='participantWalletId'/>
		</div>
		<button type='button'>Test</button>
	</form>`;

	container.insertAdjacentHTML('beforeend', html)

	const chatConversationId = container.querySelector('#joinChatConversation-chatConversationId-param');
	const participantWalletId = container.querySelector('#joinChatConversation-participantWalletId-param');

	container.querySelector('#joinChatConversation-form button').onclick = () => {
		const params = {
			chatConversationId : chatConversationId.value !== "" ? chatConversationId.value : undefined,
			participantWalletId : participantWalletId.value !== "" ? participantWalletId.value : undefined
		};

		joinChatConversation(params).then(r => r.text().then(
				t => alert(t)
			));
	};
}

export { joinChatConversation, joinChatConversationForm };