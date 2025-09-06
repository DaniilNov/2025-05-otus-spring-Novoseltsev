document.addEventListener('DOMContentLoaded', function() {
    const api = new LibraryApi();
    loadAuthors();

    async function loadAuthors() {
        try {
            const authors = await api.getAllAuthors();
            renderAuthorsTable(authors);
        } catch (error) {
            console.error('Error loading authors:', error);
        }
    }

    function renderAuthorsTable(authors) {
        const tableContainer = document.getElementById('authorsTable');
        const noAuthorsMessage = document.getElementById('noAuthorsMessage');

        if (!authors || authors.length === 0) {
            tableContainer.style.display = 'none';
            noAuthorsMessage.style.display = 'block';
            return;
        }

        noAuthorsMessage.style.display = 'none';
        tableContainer.style.display = 'block';

        let tableHtml = `
            <table class="table table-striped table-hover align-middle">
                <thead class="table-dark">
                <tr>
                    <th scope="col">ID</th>
                    <th scope="col">Full Name</th>
                </tr>
                </thead>
                <tbody>
        `;

        authors.forEach(author => {
            tableHtml += `
                <tr>
                    <td>${author.id || ''}</td>
                    <td>${author.fullName || ''}</td>
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