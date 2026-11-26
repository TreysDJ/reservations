const container = document.querySelector(".rooms-gallery");
const template = document.querySelector("#room-template");

async function loadRooms() {
    try {
        const response = await fetch(`/rooms`);

        if (!response.ok) {
            throw new Error(`HTTP ${response.status}`);
        }

        const rooms = await response.json();
        console.log("Комнаты:", rooms);

        container.replaceChildren();

        for (const room of rooms) {
            const card = template.content.cloneNode(true).firstElementChild;

            const img = card.querySelector('.room-img');
            img.src = room.img;
            img.alt = room.title;

            card.querySelector('h3').textContent = room.title;
            card.querySelector('.book-btn').href = `hotel.html?roomId=${room.roomId}`;

            container.append(card);
        }
    } catch (error) {
        console.error("Ошибка загрузки комнат:", error);
    }
}

loadRooms();