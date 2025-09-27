document.addEventListener('DOMContentLoaded', function() {
    const api = new LibraryApi();
    loadGenres();

    async function loadGenres() {
        try {
            const genres = await api.getAllGenres();
            renderGenresTable(genres);
        } catch (error) {
            console.error('Error loading genres:', error);
        }
    }

    function renderGenresTable(genres) {
        const tableContainer = document.getElementById('genresTable');
        const noGenresMessage = document.getElementById('noGenresMessage');

        if (!genres || genres.length === 0) {
            tableContainer.style.display = 'none';
            noGenresMessage.style.display = 'block';
            return;
        }

        noGenresMessage.style.display = 'none';
        tableContainer.style.display = 'block';

        let tableHtml = `
            <table class="table table-striped table-hover align-middle">
                <thead class="table-dark">
                <tr>
                    <th scope="col">ID</th>
                    <th scope="col">Name</th>
                </tr>
                </thead>
                <tbody>
        `;

        genres.forEach(genre => {
            tableHtml += `
                <tr>
                    <td>${genre.id || ''}</td>
                    <td>${genre.name || ''}</td>
                </tr>
            `;
        });

        tableHtml += `
                </tbody>
            </table>
        `;

        tableContainer.innerHTML = tableHtml;
    }
});