const announce = async (parameters) =>  {
	const baseUrl = window.location.origin;
	const url = new URL(`${window.location.pathname.split('/')[1]}/rest/announce/`, baseUrl);
	if (parameters.compact !== undefined) {
		url.searchParams.append('compact', parameters.compact);
	}

	if (parameters.downloaded !== undefined) {
		url.searchParams.append('downloaded', parameters.downloaded);
	}

	if (parameters.event !== undefined) {
		url.searchParams.append('event', parameters.event);
	}

	if (parameters.info_hash !== undefined) {
		url.searchParams.append('info_hash', parameters.info_hash);
	}

	if (parameters.latitude !== undefined) {
		url.searchParams.append('latitude', parameters.latitude);
	}

	if (parameters.left !== undefined) {
		url.searchParams.append('left', parameters.left);
	}

	if (parameters.liveness !== undefined) {
		url.searchParams.append('liveness', parameters.liveness);
	}

	if (parameters.longitude !== undefined) {
		url.searchParams.append('longitude', parameters.longitude);
	}

	if (parameters.peer_id !== undefined) {
		url.searchParams.append('peer_id', parameters.peer_id);
	}

	if (parameters.port !== undefined) {
		url.searchParams.append('port', parameters.port);
	}

	if (parameters.sign !== undefined) {
		url.searchParams.append('sign', parameters.sign);
	}

	if (parameters.uploaded !== undefined) {
		url.searchParams.append('uploaded', parameters.uploaded);
	}

	if (parameters.url !== undefined) {
		url.searchParams.append('url', parameters.url);
	}

	if (parameters.wallet_id !== undefined) {
		url.searchParams.append('wallet_id', parameters.wallet_id);
	}

	return fetch(url.toString(), {
		method: 'GET'
	});
}

const announceForm = (container) => {
	const html = `<form id='announce-form'>
		<div id='announce-compact-form-field'>
			<label for='compact'>compact</label>
			<input type='text' id='announce-compact-param' name='compact'/>
		</div>
		<div id='announce-downloaded-form-field'>
			<label for='downloaded'>downloaded</label>
			<input type='text' id='announce-downloaded-param' name='downloaded'/>
		</div>
		<div id='announce-event-form-field'>
			<label for='event'>event</label>
			<input type='text' id='announce-event-param' name='event'/>
		</div>
		<div id='announce-info_hash-form-field'>
			<label for='info_hash'>info_hash</label>
			<input type='text' id='announce-info_hash-param' name='info_hash'/>
		</div>
		<div id='announce-latitude-form-field'>
			<label for='latitude'>latitude</label>
			<input type='text' id='announce-latitude-param' name='latitude'/>
		</div>
		<div id='announce-left-form-field'>
			<label for='left'>left</label>
			<input type='text' id='announce-left-param' name='left'/>
		</div>
		<div id='announce-liveness-form-field'>
			<label for='liveness'>liveness</label>
			<input type='text' id='announce-liveness-param' name='liveness'/>
		</div>
		<div id='announce-longitude-form-field'>
			<label for='longitude'>longitude</label>
			<input type='text' id='announce-longitude-param' name='longitude'/>
		</div>
		<div id='announce-peer_id-form-field'>
			<label for='peer_id'>peer_id</label>
			<input type='text' id='announce-peer_id-param' name='peer_id'/>
		</div>
		<div id='announce-port-form-field'>
			<label for='port'>port</label>
			<input type='text' id='announce-port-param' name='port'/>
		</div>
		<div id='announce-sign-form-field'>
			<label for='sign'>sign</label>
			<input type='text' id='announce-sign-param' name='sign'/>
		</div>
		<div id='announce-uploaded-form-field'>
			<label for='uploaded'>uploaded</label>
			<input type='text' id='announce-uploaded-param' name='uploaded'/>
		</div>
		<div id='announce-url-form-field'>
			<label for='url'>url</label>
			<input type='text' id='announce-url-param' name='url'/>
		</div>
		<div id='announce-wallet_id-form-field'>
			<label for='wallet_id'>wallet_id</label>
			<input type='text' id='announce-wallet_id-param' name='wallet_id'/>
		</div>
		<button type='button'>Test</button>
	</form>`;

	container.insertAdjacentHTML('beforeend', html)

	const compact = container.querySelector('#announce-compact-param');
	const downloaded = container.querySelector('#announce-downloaded-param');
	const event = container.querySelector('#announce-event-param');
	const info_hash = container.querySelector('#announce-info_hash-param');
	const latitude = container.querySelector('#announce-latitude-param');
	const left = container.querySelector('#announce-left-param');
	const liveness = container.querySelector('#announce-liveness-param');
	const longitude = container.querySelector('#announce-longitude-param');
	const peer_id = container.querySelector('#announce-peer_id-param');
	const port = container.querySelector('#announce-port-param');
	const sign = container.querySelector('#announce-sign-param');
	const uploaded = container.querySelector('#announce-uploaded-param');
	const url = container.querySelector('#announce-url-param');
	const wallet_id = container.querySelector('#announce-wallet_id-param');

	container.querySelector('#announce-form button').onclick = () => {
		const params = {
			compact : compact.value !== "" ? compact.value : undefined,
			downloaded : downloaded.value !== "" ? downloaded.value : undefined,
			event : event.value !== "" ? event.value : undefined,
			info_hash : info_hash.value !== "" ? info_hash.value : undefined,
			latitude : latitude.value !== "" ? latitude.value : undefined,
			left : left.value !== "" ? left.value : undefined,
			liveness : liveness.value !== "" ? liveness.value : undefined,
			longitude : longitude.value !== "" ? longitude.value : undefined,
			peer_id : peer_id.value !== "" ? peer_id.value : undefined,
			port : port.value !== "" ? port.value : undefined,
			sign : sign.value !== "" ? sign.value : undefined,
			uploaded : uploaded.value !== "" ? uploaded.value : undefined,
			url : url.value !== "" ? url.value : undefined,
			wallet_id : wallet_id.value !== "" ? wallet_id.value : undefined
		};

		announce(params).then(r => r.text().then(
				t => alert(t)
			));
	};
}

export { announce, announceForm };