const createAddress = async (parameters) =>  {
	const baseUrl = window.location.origin;
	const url = new URL(`${window.location.pathname.split('/')[1]}/rest/createAddress/`, baseUrl);
	return fetch(url.toString(), {
		method: 'POST', 
		headers : new Headers({
 			'Content-Type': 'application/json'
		}),
		body: JSON.stringify({
			name : parameters.name,
			streetAddress : parameters.streetAddress,
			city : parameters.city,
			state : parameters.state,
			countryCode : parameters.countryCode,
			dialCode : parameters.dialCode,
			postalCode : parameters.postalCode,
			longitude : parameters.longitude,
			latitude : parameters.latitude,
			walletId : parameters.walletId,
			phoneNumber : parameters.phoneNumber
		})
	});
}

const createAddressForm = (container) => {
	const html = `<form id='createAddress-form'>
		<div id='createAddress-name-form-field'>
			<label for='name'>name</label>
			<input type='text' id='createAddress-name-param' name='name'/>
		</div>
		<div id='createAddress-streetAddress-form-field'>
			<label for='streetAddress'>streetAddress</label>
			<input type='text' id='createAddress-streetAddress-param' name='streetAddress'/>
		</div>
		<div id='createAddress-city-form-field'>
			<label for='city'>city</label>
			<input type='text' id='createAddress-city-param' name='city'/>
		</div>
		<div id='createAddress-state-form-field'>
			<label for='state'>state</label>
			<input type='text' id='createAddress-state-param' name='state'/>
		</div>
		<div id='createAddress-countryCode-form-field'>
			<label for='countryCode'>countryCode</label>
			<input type='text' id='createAddress-countryCode-param' name='countryCode'/>
		</div>
		<div id='createAddress-dialCode-form-field'>
			<label for='dialCode'>dialCode</label>
			<input type='text' id='createAddress-dialCode-param' name='dialCode'/>
		</div>
		<div id='createAddress-postalCode-form-field'>
			<label for='postalCode'>postalCode</label>
			<input type='text' id='createAddress-postalCode-param' name='postalCode'/>
		</div>
		<div id='createAddress-longitude-form-field'>
			<label for='longitude'>longitude</label>
			<input type='text' id='createAddress-longitude-param' name='longitude'/>
		</div>
		<div id='createAddress-latitude-form-field'>
			<label for='latitude'>latitude</label>
			<input type='text' id='createAddress-latitude-param' name='latitude'/>
		</div>
		<div id='createAddress-walletId-form-field'>
			<label for='walletId'>walletId</label>
			<input type='text' id='createAddress-walletId-param' name='walletId'/>
		</div>
		<div id='createAddress-phoneNumber-form-field'>
			<label for='phoneNumber'>phoneNumber</label>
			<input type='text' id='createAddress-phoneNumber-param' name='phoneNumber'/>
		</div>
		<button type='button'>Test</button>
	</form>`;

	container.insertAdjacentHTML('beforeend', html)

	const name = container.querySelector('#createAddress-name-param');
	const streetAddress = container.querySelector('#createAddress-streetAddress-param');
	const city = container.querySelector('#createAddress-city-param');
	const state = container.querySelector('#createAddress-state-param');
	const countryCode = container.querySelector('#createAddress-countryCode-param');
	const dialCode = container.querySelector('#createAddress-dialCode-param');
	const postalCode = container.querySelector('#createAddress-postalCode-param');
	const longitude = container.querySelector('#createAddress-longitude-param');
	const latitude = container.querySelector('#createAddress-latitude-param');
	const walletId = container.querySelector('#createAddress-walletId-param');
	const phoneNumber = container.querySelector('#createAddress-phoneNumber-param');

	container.querySelector('#createAddress-form button').onclick = () => {
		const params = {
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

		createAddress(params).then(r => r.text().then(
				t => alert(t)
			));
	};
}

export { createAddress, createAddressForm };