const updateAddress = async (parameters) =>  {
	const baseUrl = window.location.origin;
	const url = new URL(`${window.location.pathname.split('/')[1]}/rest/updateAddress/${parameters.uuid}`, baseUrl);
	return fetch(url.toString(), {
		method: 'PUT'
	});
}

const updateAddressForm = (container) => {
	const html = `<form id='updateAddress-form'>
		<div id='updateAddress-uuid-form-field'>
			<label for='uuid'>uuid</label>
			<input type='text' id='updateAddress-uuid-param' name='uuid'/>
		</div>
		<div id='updateAddress-name-form-field'>
			<label for='name'>name</label>
			<input type='text' id='updateAddress-name-param' name='name'/>
		</div>
		<div id='updateAddress-streetAddress-form-field'>
			<label for='streetAddress'>streetAddress</label>
			<input type='text' id='updateAddress-streetAddress-param' name='streetAddress'/>
		</div>
		<div id='updateAddress-city-form-field'>
			<label for='city'>city</label>
			<input type='text' id='updateAddress-city-param' name='city'/>
		</div>
		<div id='updateAddress-state-form-field'>
			<label for='state'>state</label>
			<input type='text' id='updateAddress-state-param' name='state'/>
		</div>
		<div id='updateAddress-countryCode-form-field'>
			<label for='countryCode'>countryCode</label>
			<input type='text' id='updateAddress-countryCode-param' name='countryCode'/>
		</div>
		<div id='updateAddress-dialCode-form-field'>
			<label for='dialCode'>dialCode</label>
			<input type='text' id='updateAddress-dialCode-param' name='dialCode'/>
		</div>
		<div id='updateAddress-postalCode-form-field'>
			<label for='postalCode'>postalCode</label>
			<input type='text' id='updateAddress-postalCode-param' name='postalCode'/>
		</div>
		<div id='updateAddress-longitude-form-field'>
			<label for='longitude'>longitude</label>
			<input type='text' id='updateAddress-longitude-param' name='longitude'/>
		</div>
		<div id='updateAddress-latitude-form-field'>
			<label for='latitude'>latitude</label>
			<input type='text' id='updateAddress-latitude-param' name='latitude'/>
		</div>
		<div id='updateAddress-walletId-form-field'>
			<label for='walletId'>walletId</label>
			<input type='text' id='updateAddress-walletId-param' name='walletId'/>
		</div>
		<div id='updateAddress-phoneNumber-form-field'>
			<label for='phoneNumber'>phoneNumber</label>
			<input type='text' id='updateAddress-phoneNumber-param' name='phoneNumber'/>
		</div>
		<button type='button'>Test</button>
	</form>`;

	container.insertAdjacentHTML('beforeend', html)

	const uuid = container.querySelector('#updateAddress-uuid-param');
	const name = container.querySelector('#updateAddress-name-param');
	const streetAddress = container.querySelector('#updateAddress-streetAddress-param');
	const city = container.querySelector('#updateAddress-city-param');
	const state = container.querySelector('#updateAddress-state-param');
	const countryCode = container.querySelector('#updateAddress-countryCode-param');
	const dialCode = container.querySelector('#updateAddress-dialCode-param');
	const postalCode = container.querySelector('#updateAddress-postalCode-param');
	const longitude = container.querySelector('#updateAddress-longitude-param');
	const latitude = container.querySelector('#updateAddress-latitude-param');
	const walletId = container.querySelector('#updateAddress-walletId-param');
	const phoneNumber = container.querySelector('#updateAddress-phoneNumber-param');

	container.querySelector('#updateAddress-form button').onclick = () => {
		const params = {
			uuid : uuid.value !== "" ? uuid.value : undefined,
			name : name.value !== "" ? name.value : undefined,
			streetAddress : streetAddress.value !== "" ? streetAddress.value : undefined,
			city : city.value !== "" ? city.value : undefined,
			state : state.value !== "" ? state.value : undefined,
			countryCode : countryCode.value !== "" ? countryCode.value : undefined,
			dialCode : dialCode.value !== "" ? dialCode.value : undefined,
			postalCode : postalCode.value !== "" ? postalCode.value : undefined,
			longitude : longitude.value !== "" ? longitude.value : undefined,
			latitude : latitude.value !== "" ? latitude.value : undefined,
			walletId : walletId.value !== "" ? walletId.value : undefined,
			phoneNumber : phoneNumber.value !== "" ? phoneNumber.value : undefined
		};

		updateAddress(params).then(r => r.text().then(
				t => alert(t)
			));
	};
}

export { updateAddress, updateAddressForm };