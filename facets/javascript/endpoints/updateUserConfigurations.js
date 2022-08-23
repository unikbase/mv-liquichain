const updateUserConfigurations = async (parameters) =>  {
	const baseUrl = window.location.origin;
	const url = new URL(`${window.location.pathname.split('/')[1]}/rest/updateUserConfigurations/`, baseUrl);
	return fetch(url.toString(), {
		method: 'POST', 
		headers : new Headers({
 			'Content-Type': 'application/json'
		}),
		body: JSON.stringify({
			null : parameters.null,
			null : parameters.null
		})
	});
}

const updateUserConfigurationsForm = (container) => {
	const html = `<form id='updateUserConfigurations-form'>
		<div id='updateUserConfigurations-null-form-field'>
			<label for='null'>null</label>
			<input type='text' id='updateUserConfigurations-null-param' name='null'/>
		</div>
		<div id='updateUserConfigurations-null-form-field'>
			<label for='null'>null</label>
			<input type='text' id='updateUserConfigurations-null-param' name='null'/>
		</div>
		<button type='button'>Test</button>
	</form>`;

	container.insertAdjacentHTML('beforeend', html)

	const null = container.querySelector('#updateUserConfigurations-null-param');
	const null = container.querySelector('#updateUserConfigurations-null-param');

	container.querySelector('#updateUserConfigurations-form button').onclick = () => {
		const params = {
			null : null.value !== "" ? null.value : undefined,
			null : null.value !== "" ? null.value : undefined
		};

		updateUserConfigurations(params).then(r => r.text().then(
				t => alert(t)
			));
	};
}

export { updateUserConfigurations, updateUserConfigurationsForm };