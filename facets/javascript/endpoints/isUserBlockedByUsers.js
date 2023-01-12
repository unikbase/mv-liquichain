const isUserBlockedByUsers = async (parameters) =>  {
	const baseUrl = window.location.origin;
	const url = new URL(`${window.location.pathname.split('/')[1]}/rest/isUserBlockedByUsers/`, baseUrl);
	return fetch(url.toString(), {
		method: 'POST', 
		headers : new Headers({
 			'Content-Type': 'application/json'
		}),
		body: JSON.stringify({
			walletId : parameters.walletId,
			blockers : parameters.blockers
		})
	});
}

const isUserBlockedByUsersForm = (container) => {
	const html = `<form id='isUserBlockedByUsers-form'>
		<div id='isUserBlockedByUsers-walletId-form-field'>
			<label for='walletId'>walletId</label>
			<input type='text' id='isUserBlockedByUsers-walletId-param' name='walletId'/>
		</div>
		<div id='isUserBlockedByUsers-blockers-form-field'>
			<label for='blockers'>blockers</label>
			<input type='text' id='isUserBlockedByUsers-blockers-param' name='blockers'/>
		</div>
		<button type='button'>Test</button>
	</form>`;

	container.insertAdjacentHTML('beforeend', html)

	const walletId = container.querySelector('#isUserBlockedByUsers-walletId-param');
	const blockers = container.querySelector('#isUserBlockedByUsers-blockers-param');

	container.querySelector('#isUserBlockedByUsers-form button').onclick = () => {
		const params = {
			walletId : walletId.value !== "" ? walletId.value : undefined,
			blockers : blockers.value !== "" ? blockers.value : undefined
		};

		isUserBlockedByUsers(params).then(r => r.text().then(
				t => alert(t)
			));
	};
}

export { isUserBlockedByUsers, isUserBlockedByUsersForm };