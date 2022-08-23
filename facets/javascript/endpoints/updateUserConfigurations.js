const updateUserConfigurations = async (parameters) =>  {
	const baseUrl = window.location.origin;
	const url = new URL(`${window.location.pathname.split('/')[1]}/rest/updateUserConfigurations/`, baseUrl);
	return fetch(url.toString(), {
		method: 'POST', 
		headers : new Headers({
 			'Content-Type': 'application/json'
		}),
		body: JSON.stringify({
			walletId : parameters.walletId,
			configurations : parameters.configurations
		})
	});
}

const updateUserConfigurationsForm = (container) => {
	const html = `<form id='updateUserConfigurations-form'>
		<div id='updateUserConfigurations-walletId-form-field'>
			<label for='walletId'>walletId</label>
			<input type='text' id='updateUserConfigurations-walletId-param' name='walletId'/>
		</div>
		<div id='updateUserConfigurations-configurations-form-field'>
			<label for='configurations'>configurations</label>
			<input type='text' id='updateUserConfigurations-configurations-param' name='configurations'/>
		</div>
		<button type='button'>Test</button>
	</form>`;

	container.insertAdjacentHTML('beforeend', html)

	const walletId = container.querySelector('#updateUserConfigurations-walletId-param');
	const configurations = container.querySelector('#updateUserConfigurations-configurations-param');

	container.querySelector('#updateUserConfigurations-form button').onclick = () => {
		const params = {
			walletId : walletId.value !== "" ? walletId.value : undefined,
			configurations : configurations.value !== "" ? configurations.value : undefined
		};

		updateUserConfigurations(params).then(r => r.text().then(
				t => alert(t)
			));
	};
}

export { updateUserConfigurations, updateUserConfigurationsForm };