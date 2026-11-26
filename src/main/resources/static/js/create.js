const userId = 123;

const form = document.querySelector(`form`);

form.addEventListener(`submit`, async (event) => {
    event.preventDefault();

    const submit = form.querySelector(`.submit-btn`);

    if (submit.disabled) {
        return;
    }

    submit.disabled = true;

    const formData = new FormData(form);
    const payload = {
        userId: userId,
        roomId: Number(formData.get(`room-id`)),
        startDate: formData.get(`date-from`),
        endDate: formData.get(`date-to`)
    };

    try {
        const response = await fetch(`/reservation/create`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(payload)
        });

        if (!response.ok) {
            throw new Error(`HTTP ${response.status}`)
        }

        const result = await response.json();
        alert("Успешно");
        console.log(result);
    } catch (error) {
        alert("Ошибка");
        console.error("Ошибка обновления статуса бронирования:", error);
        alert("Не удалось создать бронирование");
    } finally {
        submit.disabled = false;
    }
});
