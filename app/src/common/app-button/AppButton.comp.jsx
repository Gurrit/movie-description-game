import React from "react";
import Button from "@material-ui/core/Button";

/**
 * AppButton Component
 * 
 * Wrapper around Material-UI Button with sensible defaults.
 * 
 * @param {Object} props
 * @param {boolean} props.disabled - Whether button is disabled
 * @param {Function} props.onClick - Click handler
 * @param {string} props.variant - Button variant: "text", "outlined", "contained"
 * @param {string} props.color - Button color: "primary", "secondary", "inherit"
 * @param {string} props.children - Button content
 * @param {string} props.type - Button type: "button", "submit", "reset"
 * @param {string} props.size - Button size: "small", "medium", "large"
 */
const AppButton = ({
  disabled = false,
  onClick,
  children,
  variant = "contained",
  color = "primary",
  type = "button",
  size = "medium",
  fullWidth = false,
  ...props
}) => {
  return (
    <Button
      disabled={disabled}
      variant={variant}
      onClick={onClick}
      color={color}
      type={type}
      size={size}
      fullWidth={fullWidth}
      style={{ ...props.style }}
    >
      {children}
    </Button>
  );
};

export default AppButton;
