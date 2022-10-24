const getUserContactInfo = async (parameters) =>  {
	const baseUrl = window.location.origin;
	const url = new URL(`${window.location.pathname.split('/')[1]}/rest/getUserContactInfo/`, baseUrl);
	return fetch(url.toString(), {
		method: 'GET'
	});
}

const getUserContactInfoForm = (container) => {
	const html = `<form id='getUserContactInfo-form'>
		<button type='button'>Test</button>
	</form>`;

	container.insertAdjacentHTML('beforeend', html)


	container.querySelector('#getUserContactInfo-form button').onclick = () => {
		const params = {

		};

		getUserContactInfo(params).then(r => r.text().then(
				t => alert(t)
			));
	};
}

export { getUserContactInfo, getUserContactInfoForm };