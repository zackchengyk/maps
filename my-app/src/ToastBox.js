import React, {useEffect, useState} from "react";

const maxToasts = 4

export function ToastBox({toast}) {

  const [toastList, setToastList] = useState([])

  useEffect(() => {
    if (!toast) {
      return
    }
    setToastList((prev) => [toast, ...prev.slice(0, maxToasts - 1)])
    setTimeout(() => {
      setToastList((prev) => prev.filter((ele) => toast !== ele))
    }, 7500)
  }, [toast])

  const onClickRemoveThis = (toast) => {
    setToastList((prev) => prev.filter((ele) => toast !== ele))
  }

  return (

      <ul id="ToastContainer">
        {toastList.map((ele, i) =>
            <li className={"ToastContent " + ele.type} key={i}>
              <p>{ele.message}</p>
              <button onClick={() => onClickRemoveThis(ele)}/>
            </li>
        )}
      </ul>
  );
}