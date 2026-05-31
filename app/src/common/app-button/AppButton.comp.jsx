import React from "react";
import { Button } from "react-bootstrap";

/**
 * AppButton Component
 * 
 * Wrapper around Bootstrap Button with sensible defaults.
 * 
 * @param {Object} props
 * @param {boolean} props.disabled - Whether button is disabled
 * @param {Function} props.onClick - Click handler
 * @param {string} props.variant - Button variant: "primary", "secondary", "success", "danger", "warning", "info", "light", "dark", "outline-primary", etc.
 * @param {string} props.children - Button content
 * @param {string} props.type - Button type: "button", "submit", "reset"
 * @param {string} props.size - Button size: "sm", "lg", or default (medium)
 * @param {boolean} props.fullWidth - Whether button should be full width
 */
const AppButton = ({
  disabled = false,
  onClick,
  children,
  variant = "primary",
  type = "button",
  size,
  fullWidth = false,
  className = "",
  ...props
}) => {
  // Map size prop to Bootstrap size
  const bootstrapSize = size === "small" ? "sm" : size === "large" ? "lg" : undefined;
  
  // Map variant for compatibility
  let bootstrapVariant = variant;
  if (variant === "text") bootstrapVariant = "link";
  if (variant === "outlined" || variant === "contained") bootstrapVariant = "primary";
  
  return (
    <Button
      disabled={disabled}
      onClick={onClick}
      variant={bootstrapVariant}
      type={type}
      size={bootstrapSize}
      className={`${className} ${fullWidth ? "w-100" : ""}`}
      {...props}
    >
      {children}
    </Button>
  );
};

export default AppButton;
