const userId = 123;

const container = document.querySelector(".reservations-container");
const template = document.querySelector("#reservation-template");
const message = document.querySelector("#reservations-message");

const fmt = s => s.split('-').reverse().join('.');

loadReservations();

async function loadReservations() {
    try {
        const response = await fetch(`/reservation/list/${userId}`);

        if (!response.ok) {
            throw new Error(`HTTP ${response.status}`);
        }

        const reservations = await response.json();
        console.log("Бронирования:", reservations);
        message.textContent = `Получено бронирований: ${reservations.length}`;

        for (const reservation of reservations) {
            renderReservation(reservation);
        }
    } catch (error) {
        console.error("Ошибка загрузки бронирований:", error);
        message.textContent = "Не удалось загрузить бронирования.";
    }
}

function renderReservation(reservation) {
    const card = template.content.cloneNode(true).firstElementChild;
    const status = card.querySelector(`.res-status`);
    const approveBtn = card.querySelector(`.res-approve`);
    const cancelBtn = card.querySelector(`.res-cancel`);

    card.querySelector(".res-title").textContent = reservation.title;
    card.querySelector(".res-dates").textContent = `${fmt(reservation.startDate)} → ${fmt(reservation.endDate)}`;

    let curStatus = reservation.status;
    status.textContent = curStatus;

    function syncButtons() {
        approveBtn.disabled = curStatus !== "PENDING";
        cancelBtn.disabled = curStatus === "CANCELLED";
    }

    async function sendAction(url, nextStatus, method) {
        approveBtn.disabled = true;
        cancelBtn.disabled = true;

        try {
            const response = await fetch(url, {
                method
            });

            if (!response.ok) {
                throw new Error(`HTTP ${response.status}`)
            }

            curStatus = nextStatus;
            status.textContent = curStatus;
        } catch (error) {
            console.error("Ошибка обновления статуса бронирования:", error);
            message.textContent = "Не удалось обновить статус бронирования.";
        } finally {
            syncButtons();
        }
    }

    syncButtons();

    approveBtn.addEventListener(`click`, () => {
        sendAction(`/reservation/${reservation.id}/approve`, "APPROVED", "POST");
    })

    cancelBtn.addEventListener(`click`, () => {
        sendAction(`/reservation/${reservation.id}/cancel`, "CANCELLED", "DELETE");
    })
    container.append(card);
}

