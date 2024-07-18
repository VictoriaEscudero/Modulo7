//Validación de credenciales

$(document).ready(function () {
  $('#form-login').submit(function (event) {
//Evita el envío del formulario por defecto
    event.preventDefault();

//Obtener los valores de los campos de entrada
    let email = $('#email').val();
    let password = $('#password').val();

    $.post("/login", {email:email, password:password})
      .done(function () {
        window.location.href = '/menu';
      })
      .fail(function () {
        const alertMessage = $("#alertMessage");

        const appendAlert = (message, type) => {
          const wrapper = document.createElement('div');
          wrapper.innerHTML = [
            `<div class="alert alert-info alert-dismissible" role="alert">`,
            `   <div>${message}</div>`,
            '   <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>',
            '</div>'
          ].join('');

          alertMessage.append(wrapper);
        };

        appendAlert('Usuario o contraseña erróneo. Intenta nuevamente', 'danger');

        setTimeout(() => {
          alertMessage.fadeOut("slow", function () {alertMessage.empty();});
        }, 3000);
      });
  });
});