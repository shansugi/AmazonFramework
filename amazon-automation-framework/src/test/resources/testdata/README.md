# Test Data — orders.xlsx structure
# File: src/test/resources/testdata/orders.xlsx
#
# Sheet: OrderData
# Headers (row 1):
#   email | password | searchProduct | deliveryName | phone | address | city | state | pincode
#
# Sample rows:
# test1@qa.com | Pass@1234 | wireless mouse | John Smith | 9876543210 | 123 MG Road | Bangalore | Karnataka | 560001
# test2@qa.com | Pass@1234 | USB hub        | Jane Doe   | 8765432109 | 456 Park St | Mumbai    | Maharashtra | 400001
# test3@qa.com | Pass@1234 | laptop stand   | Bob Kumar  | 7654321098 | 789 Ring Rd | Delhi     | Delhi       | 110001
#
# ─────────────────────────────────────────────────────────────────────────────
#
# Sheet: InvalidLogins
# Headers: email | password | expectedError
#
# bad@email.com | wrongpass | incorrect
# no@exist.com  | test1234  | incorrect
#
# ─────────────────────────────────────────────────────────────────────────────
#
# File: src/test/resources/testdata/products.xlsx
#
# Sheet: SearchData
# Headers: keyword | minResults
#
# wireless earbuds | 10
# laptop stand     | 5
# USB-C cable      | 15
# phone case       | 20
# bluetooth speaker| 8
#
# ─────────────────────────────────────────────────────────────────────────────
# CREATE THE ACTUAL .xlsx FILES using Apache POI or MS Excel.
# Place them at: src/test/resources/testdata/
# The ExcelUtils class will read them automatically.
