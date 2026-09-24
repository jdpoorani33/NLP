document.addEventListener('DOMContentLoaded', function () {
    // Enable Bootstrap tooltips if present
    var tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
    tooltipTriggerList.map(function (tooltipTriggerEl) {
        return new bootstrap.Tooltip(tooltipTriggerEl);
    });

    // Client-side quick filter for ranking tables
    const tableSearch = document.getElementById('tableSearchInput');
    if (tableSearch) {
        tableSearch.addEventListener('input', function (e) {
            const term = e.target.value.toLowerCase().trim();
            const rows = document.querySelectorAll('#rankingTable tbody tr');

            rows.forEach(row => {
                const text = row.textContent.toLowerCase();
                if (text.includes(term)) {
                    row.style.display = '';
                } else {
                    row.style.display = 'none';
                }
            });
        });
    }
});

function confirmDelete(studentName) {
    return confirm("Are you sure you want to delete student '" + studentName + "'? This action cannot be undone.");
}
