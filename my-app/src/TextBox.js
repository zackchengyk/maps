function TextBox(props) {

  const label = props.label

  return (
      <div className="TextBox">
        <label htmlFor={label}> {label} </label>
        <input id={label} className={"textInput " + props.addClass}
               type={props.type} step="any" placeholder={label}
               required aria-required autoComplete="off"
               onChange={(e) =>
                   props.change(e.target.value)} value={props.value}>
        </input>
      </div>
  )
}

export default TextBox;
