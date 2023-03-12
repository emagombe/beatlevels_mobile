const get_folders = (media_list = []) => {
	const _folders = media_list.reduce((acc, item, index) => {
		const folder_path = item._data.split("/" + item._display_name)[0];
		const folder = folder_path.split("/")[folder_path.split("/").length - 1];
		if(!acc.some(_item => _item._data.split("/" + _item._display_name)[0] === folder_path)) {
			if(typeof item.bucket_display_name === "undefined") {
				acc.push({ ...item, bucket_display_name: folder, folder_path });
			} else {
				acc.push({ ...item, folder_path });
			}
		}
		return acc;
	}, []);
	_folders.sort((a, b) => a.bucket_display_name > b.bucket_display_name ? 1 : -1);
	return _folders;
};

export { get_folders };