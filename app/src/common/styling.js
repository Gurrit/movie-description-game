// Bootstrap-based styling components
// These export Bootstrap utility classes that can be used throughout the app

export const Card = ({ children, className = "", ...props }) => (
  <div className={`card bg-dark text-white ${className}`} style={{ width: "450px", margin: "auto", borderRadius: "1rem" }} {...props}>
    {children}
  </div>
);

export const CardTitle = ({ children, className = "", ...props }) => (
  <h2 className={`font-roboto ${className}`} style={{ fontSize: "3rem", color: "white" }} {...props}>
    {children}
  </h2>
);
