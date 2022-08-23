const deleteAddress = async (parameters) =>  {
	const baseUrl = window.location.origin;
	const url = new URL(`${window.location.pathname.split('/')[1]}/rest/deleteAddress/${parameters.uuid}`, baseUrl);
	return fetch(url.toString(), {
		method: 'DELETE'
	});
}

const deleteAddressForm = (container) => {
	const html = `<form id='deleteAddress-form'>
		<div id='deleteAddress-uuid-form-field'>
			<label for='uuid'>uuid</label>
			<input type='text' id='deleteAddress-uuid-param' name='uuid'/>
		</div>
		<button type='button'>Test</button>
	</form>`;

	container.insertAdjacentHTML('beforeend', html)

	const uuid = container.querySelector('#deleteAddress-uuid-param');

	container.querySelector('#deleteAddress-form button').onclick = () => {
		const params = {
			uuid : uuid.value !== "" ? uuid.value : undefined
		};

		deleteAddress(params).then(r => r.text().then(
				t => alert(t)
			));
	};
}

export { deleteAddress, deleteAddressForm };