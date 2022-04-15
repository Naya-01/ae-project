import {getSessionObject} from "../utils/session";

class ObjectLibrary {
  async getObject(id) {
    let response;
    try {
      let options = {
        method: "GET",
        headers: {
          "Content-Type": "application/json",
          "Authorization": getSessionObject("user").accessToken,
        },
      };
      response = await fetch("api/object/" + id, options);
    } catch (err) {
      console.log(err);
    }
    let actualObject;
    if (response.status === 200) {
      actualObject = await response.json();
    }
    return actualObject;

  }

  async setImage(formData, idObject) {
    let response;
    try {
      let options = {
        method: 'POST',
        body: formData,
        headers: {
          "Authorization": getSessionObject("user").accessToken,
        },
      };
      response = await fetch('api/object/setPicture/' + idObject, options)
    } catch (err) {
      console.log(err);
    }
    let newInterest;
    if (response.status === 200) {
      newInterest = await response.json();
    }
    return newInterest;
  }

  async addObject(timeSlot, description, typeName) {
    let response;
    try {
      let options = {
        method: "POST",
        body: JSON.stringify({
          "timeSlot": timeSlot,
          "object": {
            "type": {
              "idType": 0,
              "typeName": typeName,
            },
            "description": description,
            "image": null, //TODO : change the image
          }
        }),
        headers: {
          "Content-Type": "application/json",
          "Authorization": getSessionObject("user").accessToken,
        },
      };
      response = await fetch("api/object", options);
    } catch (err) {
      console.log(err);
    }
    let current_offer;
    if (response.status === 200) {
      current_offer = await response.json();
    }

    return current_offer;
  }

}

export default ObjectLibrary;