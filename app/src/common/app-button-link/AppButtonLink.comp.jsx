import AppButton from "../app-button";
import { Link } from "react-router-dom";

const AppButtonLink = ({ link, children, onClick, className = "" }) => {
  return (
    <Link to={link} className={`text-decoration-none ${className}`}>
      <AppButton onClick={onClick}>{children}</AppButton>
    </Link>
  );
};

export default AppButtonLink;
