const getAddress = async (parameters) =>  {
	const baseUrl = window.location.origin;
	const url = new URL(`${window.location.pathname.split('/')[1]}/rest/getAddress/${parameters.uuid}`, baseUrl);
	return fetch(url.toString(), {
		method: 'GET'
	});
}

const getAddressForm = (container) => {
	const html = `<form id='getAddress-form'>
		<div id='getAddress-uuid-form-field'>
			<label for='uuid'>uuid</label>
			<input type='text' id='getAddress-uuid-param' name='uuid'/>
		</div>
		<button type='button'>Test</button>
	</form>`;

	container.insertAdjacentHTML('beforeend', html)

	const uuid = container.querySelector('#getAddress-uuid-param');

	container.querySelector('#getAddress-form button').onclick = () => {
		const params = {
			uuid : uuid.value !== "" ? uuid.value : undefined
		};

		getAddress(params).then(r => r.text().then(
				t => alert(t)
			));
	};
}

export { getAddress, getAddressForm };