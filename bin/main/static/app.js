document.getElementById("checkInBtn")
  .addEventListener("click", async () => {
    const response = await fetch("/api/check-in", {
      method: "POST"
    });
    const text = await response.text();
    document.getElementById("message").innerText = text;
  });

document.getElementById("statusBtn")
  .addEventListener("click", async () => {
    const response = await fetch("/api/status");
    const text = await response.text();
    document.getElementById("message").innerText = text;
  });
