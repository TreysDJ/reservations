// Пользователь для учебного примера.
const userId = 123;

const container = document.querySelector(".reservations-container");
const template = document.querySelector("#reservation-template");
const message = document.querySelector("#reservations-message");

async function loadReservations() {
    try {
        // fetch отправляет HTTP-запрос, await ожидает ответ сервера.
        const response = await fetch(`/reservation/list/${userId}`);

        if (!response.ok) {
            throw new Error(`HTTP ${response.status}`);
        }

        // Превращаем JSON из ответа в массив JavaScript.
        const reservations = await response.json();
        console.log("Бронирования:", reservations);

        // textContent меняет текст элемента на странице.
        message.textContent = `Получено бронирований: ${reservations.length}`;

        for (const reservation of reservations) {
            const card = template.content.cloneNode(true);

            card.querySelector(".res-title").textContent = reservation.title;
            card.querySelector(".res-dates").textContent = `${reservation.startDate} - ${reservation.endDate}`;
            card.querySelector(".res-status").textContent = reservation.status;

            container.append(card);
        }
        // Задание: для каждого бронирования скопировать #reservation-template,
        // заполнить поля и добавить карточку в .reservations-container.
    } catch (error) {
        console.error("Ошибка загрузки бронирований:", error);
        message.textContent = "Не удалось загрузить бронирования.";
    }
}

// Скрипт подключен с defer, поэтому HTML уже разобран.
loadReservations();