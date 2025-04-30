package de.maxkei.components.toast;

/**
 * Defines keys for toast properties that can be used to customize the appearance and behavior of toast messages.
 */
public interface ToastClientProperties
{
    /**
     * Key for specifying the icon to be displayed in the toast.
     */
    String TOAST_ICON = "Toast.icon";

    /**
     * Key for specifying the component to be displayed in the toast.
     */
    String TOAST_COMPONENT = "Toast.component";

    /**
     * Key for specifying whether to show the close button in the toast.
     */
    String TOAST_SHOW_CLOSE_BUTTON = "Toast.showCloseButton";

    /**
     * Key for specifying the callback to be invoked when the toast is closed.
     */
    String TOAST_CLOSE_CALLBACK = "Toast.closeCallback";

    /**
     * Key for specifying the icon to be displayed for closing a toast.
     */
    String TOAST_CLOSE_ICON = "Toast.closeIcon";

    /**
     * Key for specifying the icon to be displayed for a successful toast.
     */
    String TOAST_SUCCESS_ICON = "Toast.success.icon";

    /**
     * Key for specifying the icon to be displayed for an informational toast.
     */
    String TOAST_INFO_ICON = "Toast.info.icon";

    /**
     * Key for specifying the icon to be displayed for a warning toast.
     */
    String TOAST_WARNING_ICON = "Toast.warning.icon";

    /**
     * Key for specifying the icon to be displayed for an error toast.
     */
    String TOAST_ERROR_ICON = "Toast.error.icon";
}