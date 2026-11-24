// Пользователь для учебного примера.
const userId = 123;
const message = document.querySelector("#reservations-message");

async function loadReservations() {
    try {
        // fetch отправляет HTTP-запрос, await ожидает ответ сервера.
        const response = await fetch(`/reservation/list?userId=${userId}`);

        if (!response.ok) {
            throw new Error(`HTTP ${response.status}`);
        }

        // Превращаем JSON из ответа в массив JavaScript.
        const reservations = await response.json();
        console.log("Бронирования:", reservations);

        // textContent меняет текст элемента на странице.
        message.textContent = `Получено бронирований: ${reservations.length}`;

        // Задание: для каждого бронирования скопировать #reservation-template,
        // заполнить поля и добавить карточку в .reservations-container.
    } catch (error) {
        console.error("Ошибка загрузки бронирований:", error);
        message.textContent = "Не удалось загрузить бронирования.";
    }
}

// Скрипт подключен с defer, поэтому HTML уже разобран.
loadReservations();
