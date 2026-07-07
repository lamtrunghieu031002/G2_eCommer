// import { Injectable } from '@angular/core';

// @Injectable({
//   providedIn: 'root'
// })
// export class ToastService {

//   constructor() { }

//   showToast({ error, defaultMsg: defaultMessage, title = '', delay = 5000 }: {
//     error?: any,
//     defaultMsg: string,
//     title?: string,
//     delay?: number
//   }) {
//     // Xác định message và màu sắc
//     let message = defaultMessage;
//     let toastClass = 'bg-danger'; // Mặc định cho lỗi
    
//     if (!error) {
//       toastClass = '#212529'; // Thành công
//     }

//     if (error) {
//       if (error.error && error.error.message) {
//         message = error.error.message;
//       } else if (typeof error === 'string') {
//         message = error;
//       }
//     }

//     // Tạo container nếu chưa có
//     let toastContainer = document.getElementById('toast-container');
//     if (!toastContainer) {
//       toastContainer = document.createElement('div');
//       toastContainer.id = 'toast-container';
//       toastContainer.style.position = 'fixed';
//       toastContainer.style.top = '20px';
//       toastContainer.style.right = '20px';
//       toastContainer.style.zIndex = '9999';
//       document.body.appendChild(toastContainer);
//     }

//     // Tạo toast element
//     const toast = document.createElement('div');
//     toast.classList.add('toast', 'show', toastClass, 'text-white');
//     toast.style.minWidth = '300px';
//     toast.style.marginBottom = '1rem';
//     toast.style.borderRadius = '5px';
    
//     toast.style.boxShadow = '0 2px 10px rgba(0,0,0,0.1)';

//     // Nội dung toast
//     toast.innerHTML = `
//       <div class="toast-header" style="display: flex; justify-content: space-between; align-items: center; padding: 12px; background-color: #212529">
//         <strong style="color: white">${title}</strong>
//         <button type="button" class="close-btn" style="background: none; border: none; color: white; cursor: pointer">
//           &times;
//         </button>
//       </div>
//       <div class="toast-body" style="padding: 12px; color: black">
//         ${message}
//       </div>
//     `;

//     // Thêm vào container
//     toastContainer.appendChild(toast);

//     // Tự động ẩn sau delay
//     const timeoutId = setTimeout(() => {
//       toast.remove();
//     }, delay);

//     // Xử lý đóng manual
//     toast.querySelector('.close-btn')?.addEventListener('click', () => {
//       clearTimeout(timeoutId);
//       toast.remove();
//     });
//   }
// }

import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class ToastService {

  constructor() { }

  showToast({ error, defaultMsg: defaultMessage, title = '', delay = 2000 }: {
    error?: any,
    defaultMsg: string,
    title?: string,
    delay?: number
  }) {
    let message = defaultMessage;
    let toastClass = 'toast-error';

    if (!error) {
      toastClass = 'toast-success';
    }

    if (error) {
      if (error.error && error.error.message) {
        message = error.error.message;
      } else if (typeof error === 'string') {
        message = error;
      }
    }

    let overlay = document.getElementById('toast-overlay') as HTMLElement;
    if (!overlay) {
      overlay = document.createElement('div');
      overlay.id = 'toast-overlay';
      overlay.classList.add('toast-overlay');
      document.body.appendChild(overlay);
    }

    let toastContainer = document.getElementById('toast-container') as HTMLElement;
    if (!toastContainer) {
      toastContainer = document.createElement('div');
      toastContainer.id = 'toast-container';
      toastContainer.classList.add('toast-container');
      document.body.appendChild(toastContainer);
    }

    overlay.style.display = 'block';

    const toast = document.createElement('div');
    toast.classList.add('toast', 'show', toastClass);
    toast.innerHTML = `

      <div class="toast-body">
        ${message}
      </div>
    `;

    toastContainer?.appendChild(toast);

    const removeToast = () => {
      toast.classList.add('fade-out');
      setTimeout(() => {
        toast.remove();
        if (toastContainer && !toastContainer.hasChildNodes()) {
          overlay.style.display = 'none';
        }
      }, 500);
    };

    const timeoutId = setTimeout(removeToast, delay);

    toast.querySelector('.close-btn')?.addEventListener('click', () => {
      clearTimeout(timeoutId);
      removeToast();
    });

    overlay.addEventListener('click', (event) => {
      if (event.target === overlay) {
        clearTimeout(timeoutId);
        removeToast();
      }
    });
  }
}
