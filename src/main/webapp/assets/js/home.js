// Original (unfiltered) list
let foodList = $("#food-list > div");
// List of food items to show or hide, basically a subset of the original list
let foodItems;
let sorted = null;
// Store the category id of the previous button
let prevCategoryID = null;
let showMoreButton = document.getElementById("btn-show-more");
const itemsToShow = 12;
let itemsShown = 0;

//show success order food
$(document).ready(function () {
  notSort = document.querySelectorAll("div[id^='food-']");
  showInitialFoodItems();
});

$(document).on("click", ".btn-categories", function () {
  let categoryID = $(this).data("food-type-id");
  foodList = $("#food-list > div");

  // Sort the food items if they are not sorted or the clicked button is different from the previous
  // Often this happens when the user clicks on a category for the first time, or when the user switches to another
  if (sorted === null || categoryID !== prevCategoryID) {
    // Makes a copy of the original (unsorted) list, then sorts it
    sorted = Array.from(foodList);
    sorted.sort(function (a, b) {
      let aId = a.id.substring(5);
      let bId = b.id.substring(5);
      return aId.localeCompare(bId);
    });

    // Find the img of the clicked button, and set its border to indicate the chosen category
    let img = $(this).find("img");
    img.addClass("border-4 border-primary shadow");

    // Find the img of the previous button, and remove its border
    if (prevCategoryID !== null) {
      let prevImg = $(
        ".btn-categories[data-food-type-id=" + prevCategoryID + "]"
      ).find("img");
      prevImg.removeClass("border-4 border-primary shadow");
    }

    // Store the id of the clicked button
    prevCategoryID = categoryID;

    // Hide all the food items not matching the chosen category, and show the matching ones
    for (let i = 0; i < foodList.length; i++) {
      let foodTypeID = Number.parseInt(foodList[i].id.substring(5));
      if (foodTypeID !== categoryID) {
        foodList[i].classList.add("d-none");
      } else {
        foodList[i].classList.remove("d-none");
      }
    }
    showInitialFoodItems();
  } else {
    // In the case of the same button, the list restored to its original state (not sorted)
    sorted = null;

    // Find the img of the previous button, and remove its border
    if (prevCategoryID !== null) {
      let prevImg = $(
        ".btn-categories[data-food-type-id=" + prevCategoryID + "]"
      ).find("img");
      prevImg.removeClass("border-4 border-primary shadow");
    }

    prevCategoryID = null; // Remove the id of the previous button

    // Restore the original list
    for (let i = 0; i < foodList.length; i++) {
      foodList[i].classList.remove("d-none");
    }

    // Show the first 12 items
    showInitialFoodItems();
  }
});

// Get all button addToCartBtn
var addToCartButtons = document.querySelectorAll(".addToCartBtn");

// Loop each button and add click event
addToCartButtons.forEach(function (button) {
  button.addEventListener("click", function () {
    let foodId = this.getAttribute("data-foodid");
    let quantity = this.getAttribute("data-quantity");
    // Send AJAX request to addToCart servlet endpoint
    $.ajax({
      type: "GET",
      url: "addToCart",
      data: {
        fid: foodId,
        quantity: quantity,
      },
      success: function (response) {
        console.log("Item added to cart successfully.");
      },
      error: function (error) {
        console.error("Error occurred: " + error.responseText);
      },
    });
    //    window.location.reload();
  });
});

/**
 * Show default food items
 *
 * This function selects the default food items from the food-list element
 * and hides the rest of the items.
 */
function showInitialFoodItems() {
  // Get all the food items from the food-list element
  foodItems = Array.from($("#food-list > div:not(.d-none)"));
  // Define the number of items to show
  const itemsToShow = 12;

  // Define the number of items shown
  itemsShown = foodItems.length;

  // Hide the items that exceed the number of items to show
  for (let i = itemsToShow; i < foodItems.length; i++) {
    foodItems[i].classList.add("d-none");
    itemsShown--;
  }

  // Show the "show more" button if there are more items to show
  autoHideButton();
}

// Add event listener to the "Xem thêm" button
showMoreButton.addEventListener("click", function () {
  // Stores the number of items shown before the click
  let itemsShownBeforeClick = itemsShown;

  // Show the next 12 items
  for (let i = itemsShown; i < itemsShownBeforeClick + itemsToShow; i++) {
    if (i < foodItems.length) {
      foodItems[i].classList.remove("d-none");
      itemsShown++;
    }
  }
  autoHideButton();
});

function autoHideButton() {
  // Hide the "show more" button if there are no more items to show
  if (itemsShown >= foodItems.length) {
    showMoreButton.classList.add("d-none");
  } else {
    showMoreButton.classList.remove("d-none");
  }
}


let timer;
function searchFoodByKeyword() {
  const searchInput = document.getElementById("search-bar");
  const searchResultsList = document.getElementById("search-results-list");
  const foodList = document.querySelectorAll("#food-list > div");

  // Clear previous search results and display loading message
  searchResultsList.innerHTML = "Đang tìm món...";

  if (searchInput.value.trim() === "") {
    searchResultsList.classList.remove("d-flex");
    searchResultsList.classList.add("d-none");
    return;
  }

  document.getElementsByTagName("body")[0].onclick = (e) => {
    if (e.target != searchResultsList && e.target != searchInput) {
      searchResultsList.innerHTML = "";
      searchResultsList.classList.remove("d-flex");
      searchResultsList.classList.add("d-none");
    }
  };

  // Clear previous timer if it exists
  clearTimeout(timer);
  // Starts a timer to get the search results
  timer = setTimeout(() => {
    // Perform local search on the DOM
    const searchTerm = searchInput.value.toLowerCase().trim();
    let resultsFound = false;
    searchResultsList.innerHTML = ""; // Clear previous results

    foodList.forEach((item) => {
      const foodName = item.querySelector(".card-title").textContent.toLowerCase();

      if (foodName.includes(searchTerm)) {
        // If match found, clone the item and display in search results
        const clonedItem = item.cloneNode(true);
        clonedItem.classList.add("search-results");
        searchResultsList.appendChild(clonedItem);
        resultsFound = true;
      }
    });

    if (!resultsFound) {
      searchResultsList.innerHTML = "Không tìm thấy món";
    }

    searchResultsList.classList.remove("d-none");
    searchResultsList.classList.add("d-flex");
  }, 300);
}


